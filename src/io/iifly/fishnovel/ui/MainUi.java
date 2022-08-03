package io.iifly.fishnovel.ui;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import io.iifly.fishnovel.conf.PersistentState;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class MainUi implements ToolWindowFactory, DumbAware {

    private PersistentState persistentState = PersistentState.getInstance();

    /**
     * 缓存文件页数所对应的seek，避免搜索指针的时候每次从头读取文件
     **/
    private Map<Long, Long> seekCache = new LinkedHashMap<>();

    /**
     * 缓存文件页数所对应seek的间隔
     * 该值越小，跳页时间越短，但对应的内存会增大
     **/
    private final int cacheStep = 500;

    /**
     * 读取文件路径
     **/
    private String novelPath = persistentState.getNovelPath();

    /**
     * 读取字体设置
     **/
    private String type = persistentState.getFontType();

    /**
     * 读取字号设置
     **/
    private String size = persistentState.getFontSize();

    /**
     * 读取每页行数设置
     **/
    private int lineCount = Integer.parseInt(persistentState.getLineCount());

    /**
     * 当前正在阅读页数
     **/
    private long currentPage = Long.parseLong(persistentState.getCurrentPage());

    /**
     * 正文内容显示
     **/
    private JTextArea textArea;

    /**
     * 当前阅读页&跳页输入框
     **/
    private JTextField current;

    /**
     * 显示总页数
     **/
    private JLabel total = new JLabel();

    /**
     * 是否展示
     **/
    private boolean show = true;

    private static JPanel mainPanel;
    private static JPanel operationPanel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            toolWindow.getContentManager()
                    .addContent(ContentFactory.SERVICE.getInstance().createContent(mainPanel(), "Fish-Novel", false));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化整体面板
     **/
    private JPanel mainPanel() {
        return Optional.ofNullable(mainPanel).orElseGet(() -> {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(initTextArea(), BorderLayout.CENTER);
            mainPanel.add(initOperationPanel(), BorderLayout.EAST);
            return mainPanel;
        });
    }

    /**
     * 正文区域初始化
     **/
    private JTextArea initTextArea() {
        textArea = new JTextArea();
        //初始化显示文字
        textArea.setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font(type, Font.PLAIN, Integer.parseInt(size)));
        textArea.setBorder(JBUI.Borders.empty());
        textArea.setRows(lineCount);
        textArea.setText(loadCurrentPage(currentPage));
        return textArea;
    }

    /**
     * 初始化操作面板
     **/
    private JPanel initOperationPanel() {
        // 总页数
        total.setText("/" + totalPage());

        operationPanel = new JPanel();
        operationPanel.setBorder(JBUI.Borders.empty());
        operationPanel.setPreferredSize(new Dimension(240, 30));
        operationPanel.add(initTextField(), BorderLayout.EAST);
        operationPanel.add(total, BorderLayout.EAST);
        operationPanel.add(initFreshButton(), BorderLayout.EAST);
        operationPanel.add(initUpButton(), BorderLayout.EAST);
        operationPanel.add(initDownButton(), BorderLayout.EAST);
        operationPanel.add(initBossButton(), BorderLayout.SOUTH);
        return operationPanel;
    }

    /**
     * 跳页输入框
     **/
    private JTextField initTextField() {
        current = new JTextField();
        // current.setAutoscrolls(true);
        current.setHorizontalAlignment(JTextField.RIGHT);
        current.setOpaque(false);
        current.setBorder(JBUI.Borders.empty(0));
        current.setText(String.valueOf(currentPage));
        current.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                //判断按下的键是否是回车键
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    try {
                        String input = current.getText();
                        String inputCurrent = input.trim();
                        int jump = Integer.parseInt(inputCurrent);
                        if (jump < 1) {
                            currentPage = 1;
                        } else {
                            currentPage = jump;
                            long currentLine = (jump - 1) * lineCount;
                            if (currentLine > totalLine()) {
                                currentPage = totalLine() / lineCount;
                            }
                        }
                        textArea.setText(loadCurrentPage(findSeek(currentPage)));
                    } catch (NumberFormatException e2) {
                        textArea.setText("请输入数字");
                    }

                }
            }
        });
        return current;
    }

    /**
     * 刷新按钮🔄
     **/
    private JButton initFreshButton() {
        JButton refresh = new JButton("\uD83D\uDD04");
        refresh.setPreferredSize(new Dimension(30, 30));
        refresh.setContentAreaFilled(false);
        refresh.setBorderPainted(false);
        refresh.addActionListener(e -> {
            try {
                persistentState = PersistentState.getInstanceForce();
                if (!StringUtils.equals(novelPath, persistentState.getNovelPath())) {
                    if (StringUtils.isEmpty(persistentState.getNovelPath())) {
                        return;
                    }

                    novelPath = persistentState.getNovelPath();
                    currentPage = 1;
                    persistentState.setCurrentPage("1");;
                    persistentState.setTotalLine(String.valueOf(countLine()));
                }
                if (currentPage > 1 && !StringUtils.equals(String.valueOf(lineCount), persistentState.getLineCount())) {
                    long currentLine = currentPage * lineCount - lineCount;
                    lineCount = Integer.parseInt(persistentState.getLineCount());
                    currentPage = (currentLine + lineCount) / lineCount;
                }
                seekCache.clear();
                type = persistentState.getFontType();
                size = persistentState.getFontSize();

                current.setText(String.valueOf(currentPage));
                total.setText("/" + totalPage());
                textArea.setFont(new Font(type, Font.PLAIN, Integer.parseInt(size)));
                textArea.setText(loadCurrentPage(findSeek(currentPage)));

            } catch (Exception newE) {
                newE.printStackTrace();
            }
        });
        return refresh;
    }

    /**
     * 向上翻页按钮
     **/
    private JButton initUpButton() {
        JButton prevB = new JButton("←");
        prevB.setPreferredSize(new Dimension(20, 30));
        prevB.setContentAreaFilled(false);
        prevB.setBorderPainted(false);
        prevB.addActionListener(e -> {
            if (currentPage <= 1) {
                return;
            }

            current.setText(String.valueOf(--currentPage));
            textArea.setText(loadCurrentPage(findSeek(currentPage)));
        });

        prevB.registerKeyboardAction(prevB.getActionListeners()[0],
                KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return prevB;
    }

    /**
     * 向下翻页按钮
     **/
    private JButton initDownButton() {
        JButton nextB = new JButton("→");
        nextB.setPreferredSize(new Dimension(20, 30));
        nextB.setContentAreaFilled(false);
        nextB.setBorderPainted(false);
        nextB.addActionListener(e -> {
            if (currentPage >= totalPage()) {
                return;
            }
            current.setText(String.valueOf(++currentPage));
            textArea.setText(loadCurrentPage(Long.parseLong(persistentState.getNextSeek())));
        });

        nextB.registerKeyboardAction(nextB.getActionListeners()[0],
                KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.ALT_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        return nextB;
    }

    /**
     * 隐藏按钮
     **/
    private JButton initBossButton() {
        //老板键
        JButton bossB = new JButton(show ? "✔" : "✘");
        bossB.setPreferredSize(new Dimension(20, 30));
        bossB.setContentAreaFilled(false);
        bossB.setBorderPainted(false);
        bossB.addActionListener(e -> {
            textArea.setVisible(show = !show);
            bossB.setText(show ? "✔" : "✘");
        });
        bossB.registerKeyboardAction(bossB.getActionListeners()[0],
                KeyStroke.getKeyStroke(KeyEvent.VK_SEMICOLON, InputEvent.ALT_MASK),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        return bossB;
    }

    /**
     * 向下读取文件
     **/
    private String loadCurrentPage(long targetSeek) {
        if (StringUtils.isBlank(novelPath)) {
            return "Welcome Fish Novel！\n请至 File -> Settings -> Other Settings -> Fish-Novel 设置小说路径！";
        }
        try (RandomAccessFile ra = new RandomAccessFile(novelPath, "r")) {
            persistentState.setCurrentSeek(String.valueOf(targetSeek));
            StringBuilder str = new StringBuilder();
            ra.seek(targetSeek);
            String aLine;
            for (int i = 0; i < lineCount && (aLine = ra.readLine()) != null; i++) {
                str.append(new String(aLine.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8)).append(System.lineSeparator());
            }
            persistentState.setNextSeek(String.valueOf(ra.getFilePointer()));
            persistentState.setCurrentPage(String.valueOf(currentPage));
            return str.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    /**
     * 读取文件总行数
     **/
    private long countLine() throws IOException {
        try {
            return Files.lines(Paths.get(novelPath)).count();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 找查找当前页指针位置
     **/
    private long findSeek(long page) {
        if (page <= 1) {
            return 0;
        }
        if (seekCache.containsKey(page)) {
            return seekCache.get(page);
        }
        long seek = 0;
        try (RandomAccessFile ra = new RandomAccessFile(novelPath, "r")) {
            long line = 0;
            long preCachePage = page / cacheStep * cacheStep;
            while (preCachePage > 0) {
                if (seekCache.containsKey(preCachePage)) {
                    ra.seek(seekCache.get(preCachePage));
                    line = preCachePage * lineCount;
                    break;
                }
                preCachePage -= cacheStep;
            }

            long currentLine = page * lineCount - lineCount;
            while (ra.readLine() != null) {
                line++;
                if (line % (cacheStep * lineCount) == 0) {
                    seekCache.put((line / lineCount), ra.getFilePointer());
                    // System.out.println("cache page : " + (line / lineCount) + "seek : " + ra.getFilePointer());
                }
                if (line == currentLine) {
                    seek = ra.getFilePointer();
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return seek;
    }

    private long totalPage() {
        return (totalLine() + totalLine() + 1) / 2 / lineCount;
    }
    /**
     * 当前文件总页数
     **/
    private long totalLine(){
        return Long.parseLong(persistentState.getTotalLine());
    }

}
