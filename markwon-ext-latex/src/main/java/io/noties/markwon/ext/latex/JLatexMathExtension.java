package io.noties.markwon.ext.latex;

import androidx.annotation.NonNull;

import com.vladsch.flexmark.formatter.Formatter;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererFactory;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataHolder;

import org.jetbrains.annotations.NotNull;

public class JLatexMathExtension implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension, Formatter.FormatterExtension,
        FlexmarkHtmlConverter.HtmlConverterExtension{

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
    public void extend(FlexmarkHtmlConverter.@NotNull Builder builder) {
        builder.htmlNodeRendererFactory(new JLatexNodeFactory());
    }

    @Override
    public void extend(Formatter.Builder formatterBuilder) {
        formatterBuilder.nodeFormatterFactory(new JLatexNodeFormatter.Factory());
    }

    @Override
    public void extend(@NonNull HtmlRenderer.Builder htmlRendererBuilder, @NotNull String rendererType) {
        if (htmlRendererBuilder.isRendererType("HTML")) {
            htmlRendererBuilder.nodeRendererFactory(new JLatexMathNodeRenderer.Factory());
        } else if (htmlRendererBuilder.isRendererType("JIRA")) {
            //rendererBuilder.nodeRendererFactory(new GitLabJiraRenderer.Factory());
        }
    }

    public static class JLatexNodeFactory implements HtmlNodeRendererFactory {
        @Override
        public HtmlNodeRenderer apply(DataHolder options) {
            return new JLatexNodeConverter(options);
        }
    }
}
