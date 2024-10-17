package io.noties.markwon.ext.latex;

import androidx.annotation.NonNull;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import org.jetbrains.annotations.NotNull;

public class JLatexMathExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension{

    private JLatexMathExtension() {
    }

    public static JLatexMathExtension create() {
        return new JLatexMathExtension();
    }

    @Override
    public void parserOptions(MutableDataHolder options) {

    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customInlineParserExtensionFactory(new JLatexMathInlineParser.Factory());
        parserBuilder.customBlockParserFactory(new JLatexMathBlockParser.Factory());
    }

    @Override
    public void rendererOptions(@NotNull MutableDataHolder options) {

    }

    @Override
    public void extend(@NonNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new JLatexMathNodeRenderer.Factory());
        } else if (htmlRendererBuilder.isRendererType("JIRA")) {
            //rendererBuilder.nodeRendererFactory(new GitLabJiraRenderer.Factory());
        }
    }


}
