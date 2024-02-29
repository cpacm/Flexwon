package io.noties.markwon.html.tag;

import android.text.Layout;
import android.text.style.AlignmentSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.RenderProps;
import io.noties.markwon.html.HtmlTag;

public class CenterHandler extends SimpleTagHandler {
    @Nullable
    @Override
    public Object getSpans(@NonNull MarkwonConfiguration configuration, @NonNull RenderProps renderProps, @NonNull HtmlTag tag) {
        return new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER);
    }

    @NonNull
    @Override
    public Collection<String> supportedTags() {
        return Collections.singleton("center");
    }
}
