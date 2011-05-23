package fr.mbs.vxml.utils;

import com.gargoylesoftware.htmlunit.DefaultPageCreator;
import com.gargoylesoftware.htmlunit.PageCreator;

public class VxmlDefaultPageCreator extends DefaultPageCreator implements PageCreator {
    private static final long serialVersionUID = 3094573884715849957L;

    @Override
    protected String determinePageType(String contentType) {
        return "xml";
    }
}
