/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.litho.sections.common;

import android.support.annotation.Nullable;
import com.facebook.litho.Component;
import com.facebook.litho.Diff;
import com.facebook.litho.annotations.Prop;
import com.facebook.litho.sections.ChangeSet;
import com.facebook.litho.sections.SectionContext;
import com.facebook.litho.sections.annotations.DiffSectionSpec;
import com.facebook.litho.sections.annotations.OnDiff;
import com.facebook.litho.utils.MapDiffUtils;
import com.facebook.litho.widget.ComponentRenderInfo;
import java.util.Map;

@DiffSectionSpec
public class SingleComponentSectionSpec {

  @OnDiff
  public static void onCreateChangeSet(
      SectionContext context,
      ChangeSet changeSet,
      @Prop Diff<Component> component,
      @Prop(optional = true) Diff<Boolean> sticky,
      @Prop(optional = true) Diff<Integer> spanSize,
      @Prop(optional = true) Diff<Boolean> isFullSpan,
      @Prop(optional = true) Diff<Map<String, Object>> customAttributes,
      @Prop(optional = true) Diff<Object> data) {
    final Object prevData = data.getPrevious();
    final Object nextData = data.getNext();

    if (component.getNext() == null) {
      changeSet.delete(0, prevData);
      return;
    }

    boolean isNextSticky = false;
    if (sticky != null && sticky.getNext() != null) {
      isNextSticky = sticky.getNext();
    }

    int nextSpanSize = 1;
    if (spanSize != null && spanSize.getNext() != null) {
      nextSpanSize = spanSize.getNext();
    }

    boolean isNextFullSpan = false;
    if (isFullSpan != null && isFullSpan.getNext() != null) {
      isNextFullSpan = isFullSpan.getNext();
    }

    if (component.getPrevious() == null) {
      changeSet.insert(
          0,
          addCustomAttributes(ComponentRenderInfo.create(), customAttributes.getNext())
              .component(component.getNext())
              .isSticky(isNextSticky)
              .spanSize(nextSpanSize)
              .isFullSpan(isNextFullSpan)
              .build(),
          context.getTreePropsCopy(),
          nextData);
      return;
    }

    // Check if update is required.
    boolean isPrevSticky = false;
    if (sticky != null && sticky.getPrevious() != null) {
      isPrevSticky = sticky.getPrevious();
    }

    int prevSpanSize = 1;
    if (spanSize != null && spanSize.getPrevious() != null) {
      prevSpanSize = spanSize.getPrevious();
    }

    boolean isPrevFullSpan = false;
    if (isFullSpan != null && isFullSpan.getPrevious() != null) {
      isPrevFullSpan = isFullSpan.getPrevious();
    }

    final boolean customAttributesEqual =
        MapDiffUtils.areMapsEqual(customAttributes.getPrevious(), customAttributes.getNext());

    if (isPrevSticky != isNextSticky
        || prevSpanSize != nextSpanSize
        || isPrevFullSpan != isNextFullSpan
        || !component.getPrevious().isEquivalentTo(component.getNext())
        || !customAttributesEqual) {
      changeSet.update(
          0,
          addCustomAttributes(ComponentRenderInfo.create(), customAttributes.getNext())
              .component(component.getNext())
              .isSticky(isNextSticky)
              .spanSize(nextSpanSize)
              .isFullSpan(isNextFullSpan)
              .build(),
          context.getTreePropsCopy(),
          prevData,
          nextData);
    }
  }

  private static ComponentRenderInfo.Builder addCustomAttributes(
      ComponentRenderInfo.Builder builder, @Nullable Map<String, Object> attributes) {
    if (attributes == null) {
      return builder;
    }

    for (Map.Entry<String, Object> entry : attributes.entrySet()) {
      builder.customAttribute(entry.getKey(), entry.getValue());
    }

    return builder;
  }
}
