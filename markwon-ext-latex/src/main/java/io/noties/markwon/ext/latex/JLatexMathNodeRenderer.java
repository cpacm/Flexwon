package io.noties.markwon.ext.latex;

import com.vladsch.flexmark.html.HtmlRendererOptions;
import com.vladsch.flexmark.html.HtmlWriter;
import com.vladsch.flexmark.html.renderer.NodeRenderer;
import com.vladsch.flexmark.html.renderer.NodeRendererContext;
import com.vladsch.flexmark.html.renderer.NodeRendererFactory;
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.html.Attribute;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class JLatexMathNodeRenderer implements NodeRenderer {

    public JLatexMathNodeRenderer(DataHolder options) {
    }

    @Override
    public @Nullable Set<NodeRenderingHandler<?>> getNodeRenderingHandlers() {
        Set<NodeRenderingHandler<?>> set = new HashSet<>();

        set.add(new NodeRenderingHandler<>(JLatexInlineMath.class, JLatexMathNodeRenderer.this::render));
        set.add(new NodeRenderingHandler<>(JLatexMathBlock.class, JLatexMathNodeRenderer.this::render));
        return set;
    }

    private void render(JLatexInlineMath node, NodeRendererContext context, HtmlWriter html) {
        html.withAttr().attr(Attribute.CLASS_ATTR, "katex").withAttr().tag("span");
        html.text(node.getText());
        html.tag("/span");
    }

    private void render(JLatexMathBlock node, NodeRendererContext context, HtmlWriter html) {
        HtmlRendererOptions htmlOptions = context.getHtmlOptions();

        html.line();
        html.srcPosWithTrailingEOL(node.getChars()).attr(Attribute.CLASS_ATTR, "katex").withAttr().tag("div").line().openPre();
        html.text(node.getContentChars().normalizeEOL());
        html.closePre().tag("/div");

        html.lineIf(htmlOptions.htmlBlockCloseTagEol);
    }

    public static class Factory implements NodeRendererFactory {
        @NotNull
        @Override
        public NodeRenderer apply(@NotNull DataHolder options) {
            return new JLatexMathNodeRenderer(options);
        }
    }
}
