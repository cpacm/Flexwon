package io.noties.markwon.ext.latex;

import com.vladsch.flexmark.formatter.MarkdownWriter;
import com.vladsch.flexmark.formatter.NodeFormatter;
import com.vladsch.flexmark.formatter.NodeFormatterContext;
import com.vladsch.flexmark.formatter.NodeFormatterFactory;
import com.vladsch.flexmark.formatter.NodeFormattingHandler;
import com.vladsch.flexmark.util.data.DataHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class JLatexNodeFormatter implements NodeFormatter {
    @Override
    public @Nullable Set<NodeFormattingHandler<?>> getNodeFormattingHandlers() {
        HashSet<NodeFormattingHandler<?>> set = new HashSet<>();
        set.add(new NodeFormattingHandler<>(JLatexMathBlock.class, this::renderJLaxtexMathBlock));
        set.add(new NodeFormattingHandler<>(JLatexInlineMath.class, this::renderJLaxtexInlineMath));
        return set;
    }

    @Override
    public @Nullable Set<Class<?>> getNodeClasses() {
        return null;
    }

    private void renderJLaxtexMathBlock(JLatexMathBlock node, NodeFormatterContext context, MarkdownWriter markdown) {
        markdown.line();
        markdown.append("$$");
        markdown.line();
        String content = node.getContentChars().toString();
        markdown.append(content);
        markdown.line();
        markdown.append("$$");
        markdown.line();
    }

    private void renderJLaxtexInlineMath(JLatexInlineMath node, NodeFormatterContext context, MarkdownWriter markdown) {
        String content = node.getText().toString();
        // escape dollar signs inside inline math
        markdown.append("$`").append(content).append("`$");
    }

    static class Factory implements NodeFormatterFactory {

        @Override
        public @NotNull NodeFormatter create(@NotNull DataHolder options) {
            return new JLatexNodeFormatter();
        }
    }
}
