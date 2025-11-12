package io.noties.markwon.ext.latex;

import com.vladsch.flexmark.html2md.converter.ExtensionConversion;
import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter;
import com.vladsch.flexmark.html2md.converter.HtmlConverterOptions;
import com.vladsch.flexmark.html2md.converter.HtmlMarkdownWriter;
import com.vladsch.flexmark.html2md.converter.HtmlNodeConverterContext;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRenderer;
import com.vladsch.flexmark.html2md.converter.HtmlNodeRendererHandler;
import com.vladsch.flexmark.util.data.DataHolder;

import org.jsoup.nodes.Element;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class JLatexNodeConverter implements HtmlNodeRenderer {

    ExtensionConversion extMath;

    public JLatexNodeConverter(DataHolder options) {
        HtmlConverterOptions myHtmlConverterOptions = new HtmlConverterOptions(options);
        this.extMath = myHtmlConverterOptions.extMath;
    }

    @Override
    public Set<HtmlNodeRendererHandler<?>> getHtmlNodeRendererHandlers() {
        return new HashSet<>(Arrays.asList(
                new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.MATH_NODE, Element.class, this::processMath),
                new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.SPAN_NODE, Element.class, this::processSpanOrDiv),
                new HtmlNodeRendererHandler<>(FlexmarkHtmlConverter.DIV_NODE, Element.class, this::processSpanOrDiv)
        ));
    }


    private void processMath(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        boolean noWraps = extMath.isTextOnly();
        if (noWraps) {
            context.processTextNodes(element, false);
            return;
        }
        context.processConditional(extMath, element, () -> {
            // => $$\ncontent\n$$
            out.line();
            out.append("$$");
            out.line();
            context.processTextNodes(element, false, "", "\n");
            out.append("$$");
            out.line();
        });
    }

    private void processSpanOrDiv(Element element, HtmlNodeConverterContext context, HtmlMarkdownWriter out) {
        Set<String> classes = element.classNames();
        boolean containMathClass = false;
        for (String c : classes) {
            if (c.contains("katex") || c.contains("math") || c.contains("latex") || c.contains("mathjax")) {
                containMathClass = true;
                break;
            }
        }
        boolean noWraps = extMath.isTextOnly();
        if (containMathClass && !noWraps) {
            // if element is div
            if (element.tagName().equals("span")) {
                context.processConditional(extMath, element, () -> {
                    context.processTextNodes(element, false, "$`", "`$");
                });
                return;
            } else {
                context.processConditional(extMath, element, () -> {
                    // => $$\ncontent\n$$
                    out.line();
                    out.append("$$\n");
                    context.processTextNodes(element, false, "", "\n");
                    out.append("$$");
                });
                return;
            }
        }

        context.renderDefault(element);
    }
}
