package io.noties.markwon.ext.latex;

import com.vladsch.flexmark.ast.CodeBlock;
import com.vladsch.flexmark.ast.Text;
import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.AbstractBlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.BlockParser;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.parser.core.BlockQuoteParser;
import com.vladsch.flexmark.parser.core.HeadingParser;
import com.vladsch.flexmark.parser.core.HtmlBlockParser;
import com.vladsch.flexmark.parser.core.IndentedCodeBlockParser;
import com.vladsch.flexmark.parser.core.ListBlockParser;
import com.vladsch.flexmark.parser.core.ThematicBreakParser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;
import com.vladsch.flexmark.util.sequence.SegmentedSequence;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JLatexMathBlockParser extends AbstractBlockParser {

    final private static Pattern OPENING_MATH = Pattern.compile("^\\${2,}(?!.*$)");
    final private static Pattern CLOSING_MATH = Pattern.compile("^\\${2,}(?!.*$)(?=[ \t]*$)");

    final private JLatexMathBlock block = new JLatexMathBlock();
    private BlockContent content = new BlockContent();
    final private char mathChar;
    final private int mathLength;
    final private int mathIndent;
    final private int mathMarkerIndent;

    public JLatexMathBlockParser(DataHolder options, char mathChar, int mathLength, int mathIndent, int mathMarkerIndent) {
        this.mathChar = mathChar;
        this.mathLength = mathLength;
        this.mathIndent = mathIndent;
        this.mathMarkerIndent = mathIndent + mathMarkerIndent;
    }

    @Override
    public Block getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        int nextNonSpace = state.getNextNonSpaceIndex();
        int newIndex = state.getIndex();
        BasedSequence line = state.getLine();
        Matcher matcher;
        boolean matches = (state.getIndent() <= 3 &&
                nextNonSpace < line.length() &&
                line.charAt(nextNonSpace) == mathChar);

        if (matches) {
            BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
            matcher = CLOSING_MATH.matcher(trySequence);
            if (matcher.find()) {
                int foundFenceLength = matcher.group(0).length();

                if (foundFenceLength >= mathLength) {
                    block.setClosingMarker(trySequence.subSequence(0, foundFenceLength));
                    // closing fence - we're at end of line, so we can finalize now
                    return BlockContinue.finished();
                }
            }
        }
        // skip optional spaces of fence indent
        int i = mathIndent;
        while (i > 0 && newIndex < line.length() && line.charAt(newIndex) == ' ') {
            newIndex++;
            i--;
        }
        return BlockContinue.atIndex(newIndex);
    }

    @Override
    public void addLine(ParserState state, BasedSequence line) {
        content.add(line, state.getIndent());
    }

    @Override
    public boolean isPropagatingLastBlankLine(BlockParser lastMatchedBlockParser) {
        return false;
    }

    @Override
    public void closeBlock(ParserState state) {
// first line, if not blank, has the info string
        List<BasedSequence> lines = content.getLines();
        if (lines.size() > 0) {
            BasedSequence chars = content.getSpanningChars();
            BasedSequence spanningChars = chars.baseSubSequence(chars.getStartOffset(), lines.get(0).getEndOffset());

            if (lines.size() > 1) {
                // have more lines
                List<BasedSequence> segments = lines.subList(1, lines.size());
                block.setContent(spanningChars, segments);
                CodeBlock codeBlock = new CodeBlock();
                codeBlock.setContent(segments);
                codeBlock.setCharsFromContent();
                block.appendChild(codeBlock);
            } else {
                block.setContent(spanningChars, BasedSequence.EMPTY_LIST);
            }
        } else {
            block.setContent(content);
        }

        block.setCharsFromContent();
        content = null;
    }

    public static class Factory implements CustomBlockParserFactory {

        @Override
        public @NotNull BlockParserFactory apply(@NotNull DataHolder options) {
            return new BlockFactory(options);
        }

        @Override
        public @Nullable Set<Class<?>> getAfterDependents() {
            return new HashSet<>(Arrays.asList(
                    BlockQuoteParser.Factory.class,
                    HeadingParser.Factory.class));
        }

        @Override
        public @Nullable Set<Class<?>> getBeforeDependents() {
            return new HashSet<>(Arrays.asList(
                    HtmlBlockParser.Factory.class,
                    ThematicBreakParser.Factory.class,
                    ListBlockParser.Factory.class,
                    IndentedCodeBlockParser.Factory.class
            ));
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }
    }

    private static class BlockFactory extends AbstractBlockParserFactory {

        private BlockFactory(DataHolder options) {
            super(options);
        }

        @Override
        public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
            int nextNonSpace = state.getNextNonSpaceIndex();
            BasedSequence line = state.getLine();
            Matcher matcher;
            if (state.getIndent() < 4) {
                BasedSequence trySequence = line.subSequence(nextNonSpace, line.length());
                if ((matcher = OPENING_MATH.matcher(trySequence)).find()) {
                    int mathLength = matcher.group(0).length();
                    char mathChar = matcher.group(0).charAt(0);
                    JLatexMathBlockParser blockParser = new JLatexMathBlockParser(state.getProperties(), mathChar, mathLength, state.getIndent(), nextNonSpace);
                    blockParser.block.setOpeningMarker(trySequence.subSequence(0, mathLength));
                    return BlockStart.of(blockParser).atIndex(nextNonSpace + mathLength);
                }
            }
            return BlockStart.none();
        }
    }

}

