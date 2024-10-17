package io.noties.markwon.ext.latex;

import androidx.annotation.NonNull;

import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.ast.DoNotDecorate;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JLatexMathBlock extends Block implements DoNotDecorate {

    private BasedSequence openingMarker = BasedSequence.NULL;
    private BasedSequence closingMarker = BasedSequence.NULL;

    @NonNull
    @Override
    public @NotNull BasedSequence[] getSegments() {
        return EMPTY_SEGMENTS;
    }

    public JLatexMathBlock() {
    }

    public JLatexMathBlock(BasedSequence chars) {
        super(chars);
    }

    public JLatexMathBlock(BasedSequence chars, List<BasedSequence> segments) {
        super(chars, segments);
    }

    public JLatexMathBlock(BlockContent blockContent) {
        super(blockContent);
    }

    public BasedSequence getOpeningMarker() {
        return openingMarker;
    }

    public void setOpeningMarker(BasedSequence openingMarker) {
        this.openingMarker = openingMarker;
    }

    public BasedSequence getClosingMarker() {
        return closingMarker;
    }

    public void setClosingMarker(BasedSequence closingMarker) {
        this.closingMarker = closingMarker;
    }
}
