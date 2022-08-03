package io.iifly.fishnovel.conf;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;


@State(
        name = "PersistentState",
        storages = {@Storage(
                value = "fish-novel.xml"
        )}
)
public class PersistentState implements PersistentStateComponent<Element> {

    private static PersistentState persistentState;

    private String novelPath;

    private String fontSize;

    private String fontType;

    private String lineCount;

    private String currentPage;

    private String totalLine;

    private String currentSeek;

    private String nextSeek;



    public PersistentState() {
    }

    public static PersistentState getInstance() {
        if (persistentState == null) {
            persistentState = ServiceManager.getService(PersistentState.class);
        }
        return persistentState;
    }

    public static PersistentState getInstanceForce() {
        return ServiceManager.getService(PersistentState.class);
    }


    @Nullable
    @Override
    public Element getState() {
        Element element = new Element("PersistentState");
        element.setAttribute("novelPath", this.getNovelPath());
        element.setAttribute("fontSize", this.getFontSize());
        element.setAttribute("fontType", this.getFontType());
        element.setAttribute("lineCount", this.getLineCount());
        element.setAttribute("currentPage", this.getCurrentPage());
        element.setAttribute("totalLine", this.getTotalLine());
        element.setAttribute("currentSeek", this.getCurrentSeek());
        element.setAttribute("nextSeek", this.getNextSeek());
        return element;
    }

    @Override
    public void loadState(@NotNull Element state) {
        this.setNovelPath(state.getAttributeValue("novelPath"));
        this.setFontSize(state.getAttributeValue("fontSize"));
        this.setFontType(state.getAttributeValue("fontType"));
        this.setLineCount(state.getAttributeValue("lineCount"));
        this.setCurrentPage(state.getAttributeValue("currentPage"));
        this.setTotalLine(state.getAttributeValue("totalLine"));
        this.setCurrentSeek(state.getAttributeValue("currentSeek"));
        this.setNextSeek(state.getAttributeValue("nextSeek"));
    }

    @Override
    public void noStateLoaded() {

    }

    public String getNovelPath() {
        return novelPath = StringUtils.isEmpty(novelPath) ? "" : novelPath;
    }

    public void setNovelPath(String novelPath) {
        this.novelPath = novelPath;
    }

    public String getPre() {
        return "Alt + K";
    }

    public String getNext() {
        return "Alt + J";
    }

    public String getBoos() {
        return "Alt + ;";
    }

    public String getCurrentPage() {
        return currentPage = StringUtils.isEmpty(currentPage) ? "0" : currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getFontSize() {
        return fontSize = StringUtils.isEmpty(fontSize) ? "12" : fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontType() {
        return fontType = StringUtils.isEmpty(fontType) ? GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()[0] : fontType;
    }

    public void setFontType(String fontType) {
        this.fontType = fontType;
    }

    public String getLineCount() {
        return lineCount = StringUtils.isEmpty(lineCount) ? "1" : lineCount;
    }

    public void setLineCount(String lineCount) {
        this.lineCount = lineCount;
    }

    public String getTotalLine() {
        return totalLine = StringUtils.isEmpty(totalLine) ? "0" : totalLine;
    }

    public void setTotalLine(String totalLine) {
        this.totalLine = totalLine;
    }

    public String getCurrentSeek() {
        return currentSeek = StringUtils.isEmpty(currentSeek) ? "0" : currentSeek;
    }

    public void setCurrentSeek(String currentSeek) {
        this.currentSeek = currentSeek;
    }

    public String getNextSeek() {
        return nextSeek = StringUtils.isEmpty(nextSeek) ? "0" : nextSeek;
    }

    public void setNextSeek(String nextSeek) {
        this.nextSeek = nextSeek;
    }
}