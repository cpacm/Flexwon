package io.noties.markwon.ext.tables;

import android.text.Spanned;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.vladsch.flexmark.ext.tables.TableBlock;
import com.vladsch.flexmark.ext.tables.TableBody;
import com.vladsch.flexmark.ext.tables.TableCaption;
import com.vladsch.flexmark.ext.tables.TableCell;
import com.vladsch.flexmark.ext.tables.TableHead;
import com.vladsch.flexmark.ext.tables.TableRow;
import com.vladsch.flexmark.ext.tables.TableSeparator;
import com.vladsch.flexmark.ext.tables.TableVisitor;
import com.vladsch.flexmark.ext.tables.TableVisitorExt;
import com.vladsch.flexmark.util.ast.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;

/**
 * A class to parse <code>TableBlock</code> and return a data-structure that is not dependent
 * on commonmark-java table extension. Can be useful when rendering tables require special
 * handling (multiple views, specific table view) for example when used with `markwon-recycler` artifact
 *
 * @see #parse(Markwon, TableBlock)
 * @since 3.0.0
 */
public class Table {

    /**
     * Factory method to obtain an instance of {@link Table}
     *
     * @param markwon    Markwon
     * @param tableBlock TableBlock to parse
     * @return parsed {@link Table} or null
     */
    @Nullable
    public static Table parse(@NonNull Markwon markwon, @NonNull TableBlock tableBlock) {

        final Table table;

        final ParseVisitor visitor = new ParseVisitor(markwon);
        visitor.visit(tableBlock);

        final List<Row> rows = visitor.rows();

        if (rows == null) {
            table = null;
        } else {
            table = new Table(rows);
        }

        return table;
    }

    public static class Row {

        private final boolean isHeader;
        private final List<Column> columns;

        public Row(
                boolean isHeader,
                @NonNull List<Column> columns) {
            this.isHeader = isHeader;
            this.columns = columns;
        }

        public boolean header() {
            return isHeader;
        }

        @NonNull
        public List<Column> columns() {
            return columns;
        }

        @Override
        public String toString() {
            return "Row{" +
                    "isHeader=" + isHeader +
                    ", columns=" + columns +
                    '}';
        }
    }

    public static class Column {

        private final Alignment alignment;
        private final Spanned content;

        public Column(@NonNull Alignment alignment, @NonNull Spanned content) {
            this.alignment = alignment;
            this.content = content;
        }

        @NonNull
        public Alignment alignment() {
            return alignment;
        }

        @NonNull
        public Spanned content() {
            return content;
        }

        @Override
        public String toString() {
            return "Column{" +
                    "alignment=" + alignment +
                    ", content=" + content +
                    '}';
        }
    }

    public enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }

    private final List<Row> rows;

    public Table(@NonNull List<Row> rows) {
        this.rows = rows;
    }

    @NonNull
    public List<Row> rows() {
        return rows;
    }

    @Override
    public String toString() {
        return "Table{" +
                "rows=" + rows +
                '}';
    }

    static class ParseVisitor extends NodeVisitor implements TableVisitor {

        private final Markwon markwon;

        private List<Row> rows;

        private List<Column> pendingRow;
        private boolean pendingRowIsHeader;

        ParseVisitor(@NonNull Markwon markwon) {
            this.markwon = markwon;
            addHandlers(TableVisitorExt.VISIT_HANDLERS(this));
        }

        @Nullable
        public List<Row> rows() {
            return rows;
        }

        @Override
        public void visit(TableCell cell) {
            if (pendingRow == null) {
                pendingRow = new ArrayList<>(2);
            }

            pendingRow.add(new Table.Column(alignment(cell.getAlignment()), markwon.render(cell)));
            pendingRowIsHeader = cell.isHeader();
        }

        @Override
        public void visit(TableCaption node) {
            visitChildren(node);
        }

        @Override
        public void visit(TableBlock node) {
            visitChildren(node);
        }

        @Override
        public void visit(TableHead head) {
            visitChildren(head);

            // this can happen, ignore such row
            if (pendingRow != null && pendingRow.size() > 0) {
                if (rows == null) {
                    rows = new ArrayList<>(2);
                }

                rows.add(new Table.Row(pendingRowIsHeader, pendingRow));
            }

            pendingRow = null;
            pendingRowIsHeader = false;
        }

        @Override
        public void visit(TableSeparator node) {
            // 用于确定cell的对齐方式，不做渲染
        }

        @Override
        public void visit(TableBody node) {
            visitChildren(node);
        }

        @Override
        public void visit(TableRow row) {
            visitChildren(row);

            // this can happen, ignore such row
            if (pendingRow != null && pendingRow.size() > 0) {
                if (rows == null) {
                    rows = new ArrayList<>(2);
                }

                rows.add(new Table.Row(pendingRowIsHeader, pendingRow));
            }

            pendingRow = null;
            pendingRowIsHeader = false;
        }

        @NonNull
        private static Table.Alignment alignment(@NonNull TableCell.Alignment alignment) {
            final Table.Alignment out;
            if (TableCell.Alignment.RIGHT == alignment) {
                out = Table.Alignment.RIGHT;
            } else if (TableCell.Alignment.CENTER == alignment) {
                out = Table.Alignment.CENTER;
            } else {
                out = Table.Alignment.LEFT;
            }
            return out;
        }

    }
}
