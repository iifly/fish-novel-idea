package io.iifly.fishnovel.conf;

import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import io.iifly.fishnovel.ui.SettingUi;

import javax.swing.*;

public class FishNovelConf implements SearchableConfigurable {


    @SuppressWarnings("FieldCanBeLocal")
    private final Project project;
    private SettingUi settingUi;
    private PersistentState persistentState = PersistentState.getInstance();


    public FishNovelConf(@NotNull Project project) {
        this.project = project;
    }

    @NotNull
    @Override
    public String getId() {
        return "novel.id";
    }

    @Nullable
    @Override
    public Runnable enableSearch(String option) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Fish-Novel";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Nullable
    @Override
    public JComponent createComponent() {

        if (settingUi == null) {
            settingUi = new SettingUi();
        }
        settingUi.reload(persistentState);

        return settingUi.mainPanel;

    }

    @Override
    public boolean isModified() {
        return !StringUtils.equals(persistentState.getNovelPath(), settingUi.novelPath.getText())
                || !StringUtils.equals(persistentState.getFontSize(), settingUi.fontSize.getSelectedItem().toString())
                || !StringUtils.equals(persistentState.getPre(), settingUi.pre.getText())
                || !StringUtils.equals(persistentState.getNext(), settingUi.next.getText())
                || !StringUtils.equals(persistentState.getLineCount(), settingUi.lineCount.getSelectedItem().toString())
                || !StringUtils.equals(persistentState.getFontType(), settingUi.fontType.getSelectedItem().toString());

    }

    @Override
    public void apply() {
        persistentState.setNovelPath(settingUi.novelPath.getText());
        persistentState.setFontSize(settingUi.fontSize.getSelectedItem().toString());
        persistentState.setLineCount(settingUi.lineCount.getSelectedItem().toString());
        persistentState.setFontType(settingUi.fontType.getSelectedItem().toString());
    }

    @Override
    public void reset() {

    }

    @Override
    public void disposeUIResources() {

    }
}