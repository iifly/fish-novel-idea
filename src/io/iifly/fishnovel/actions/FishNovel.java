package io.iifly.fishnovel.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;
import io.iifly.fishnovel.ui.MainUi;

/**
 * @author zh-hq
 * @Description
 * @date 2022/7/31
 */
public class FishNovel extends AnAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() != null) {
            // 将项目对象，ToolWindow的id传入，获取控件对象
            ToolWindow fishNovel = ToolWindowManager.getInstance(e.getProject()).getToolWindow("fish-novel");
            if (fishNovel != null) {
                // 无论当前状态为关闭/打开，进行强制打开ToolWindow
                fishNovel.show(() -> {

                });
                if (fishNovel.getContentManager().getContentCount() < 1) {
                    MainUi mainUi = new MainUi();
                    mainUi.createToolWindowContent(e.getProject(), fishNovel);
                }
            }
        }
    }
}
