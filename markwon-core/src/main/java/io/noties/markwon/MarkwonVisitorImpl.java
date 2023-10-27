package io.noties.markwon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.ast.VisitHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 3.0.0
 */
class MarkwonVisitorImpl extends com.vladsch.flexmark.util.ast.NodeVisitor implements MarkwonVisitor {

    private final MarkwonConfiguration configuration;

    private final RenderProps renderProps;

    private final SpannableBuilder builder;

    private final Map<Class<? extends Node>, NodeVisitor<? extends Node>> nodes;

    // @since 4.3.0
    private final BlockHandler blockHandler;

    MarkwonVisitorImpl(
            @NonNull MarkwonConfiguration configuration,
            @NonNull RenderProps renderProps,
            @NonNull SpannableBuilder builder,
            @NonNull Map<Class<? extends Node>, NodeVisitor<? extends Node>> nodes,
            @NonNull BlockHandler blockHandler) {
        this.configuration = configuration;
        this.renderProps = renderProps;
        this.builder = builder;
        this.nodes = nodes;
        this.blockHandler = blockHandler;

        ArrayList<VisitHandler<?>> handlers = new ArrayList<>();
        for (Class<? extends Node> nodeClass : nodes.keySet()) {
            handlers.add(new VisitHandler<>(nodeClass, this::visitImpl));
        }
        addHandlers(handlers.toArray(new VisitHandler<?>[]{}));

        //addHandlers(BlockVisitorExt.VISIT_HANDLERS(this));
        //addHandlers(InlineVisitorExt.VISIT_HANDLERS(this));
    }

    private void visitImpl(@NonNull Node node) {
        //noinspection unchecked
        final NodeVisitor<Node> nodeVisitor = (NodeVisitor<Node>) nodes.get(node.getClass());
        if (nodeVisitor != null) {
            nodeVisitor.visit(this, node);
        } else {
            visitChildren(node);
        }
    }

    @NonNull
    @Override
    public MarkwonConfiguration configuration() {
        return configuration;
    }

    @NonNull
    @Override
    public RenderProps renderProps() {
        return renderProps;
    }

    @NonNull
    @Override
    public SpannableBuilder builder() {
        return builder;
    }


    @Override
    public boolean hasNext(@NonNull Node node) {
        return node.getNext() != null;
    }

    @Override
    public void ensureNewLine() {
        if (builder.length() > 0
                && '\n' != builder.lastChar()) {
            builder.append('\n');
        }
    }

    @Override
    public void forceNewLine() {
        builder.append('\n');
    }

    @Override
    public int length() {
        return builder.length();
    }

    @Override
    public void setSpans(int start, @Nullable Object spans) {
        SpannableBuilder.setSpans(builder, spans, start, builder.length());
    }

    @Override
    public void clear() {
        renderProps.clearAll();
        builder.clear();
    }

    @Override
    public <N extends Node> void setSpansForNode(@NonNull N node, int start) {
        setSpansForNode(node.getClass(), start);
    }

    @Override
    public <N extends Node> void setSpansForNode(@NonNull Class<N> node, int start) {
        setSpans(start, configuration.spansFactory().require(node).getSpans(configuration, renderProps));
    }

    @Override
    public <N extends Node> void setSpansForNodeOptional(@NonNull N node, int start) {
        setSpansForNodeOptional(node.getClass(), start);
    }

    @Override
    public <N extends Node> void setSpansForNodeOptional(@NonNull Class<N> node, int start) {
        final SpanFactory factory = configuration.spansFactory().get(node);
        if (factory != null) {
            setSpans(start, factory.getSpans(configuration, renderProps));
        }
    }

    @Override
    public void blockStart(@NonNull Node node) {
        blockHandler.blockStart(this, node);
    }

    @Override
    public void blockEnd(@NonNull Node node) {
        blockHandler.blockEnd(this, node);
    }

    static class BuilderImpl implements Builder {

        private final Map<Class<? extends Node>, NodeVisitor<? extends Node>> nodes = new HashMap<>();
        private BlockHandler blockHandler;

        @NonNull
        @Override
        public <N extends Node> Builder on(@NonNull Class<N> node, @Nullable NodeVisitor<? super N> nodeVisitor) {

            // @since 4.1.1 we might actually introduce a local flag to check if it's been built
            //  and throw an exception here if some modification is requested
            //  NB, as we might be built from different threads this flag must be synchronized

            // we should allow `null` to exclude node from being visited (for example to disable
            // some functionality)
            if (nodeVisitor == null) {
                nodes.remove(node);
            } else {
                nodes.put(node, nodeVisitor);
            }
            return this;
        }

        @NonNull
        @Override
        public Builder blockHandler(@NonNull BlockHandler blockHandler) {
            this.blockHandler = blockHandler;
            return this;
        }

        @NonNull
        @Override
        public MarkwonVisitor build(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps renderProps) {
            // @since 4.3.0
            BlockHandler blockHandler = this.blockHandler;
            if (blockHandler == null) {
                blockHandler = new BlockHandlerDef();
            }

            return new MarkwonVisitorImpl(
                    configuration,
                    renderProps,
                    new SpannableBuilder(),
                    Collections.unmodifiableMap(nodes),
                    blockHandler);
        }
    }
}
