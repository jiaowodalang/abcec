/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.data.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.apache.druid.data.input.impl.CombiningInputSource;
import org.apache.druid.data.input.impl.HttpInputSource;
import org.apache.druid.data.input.impl.InlineInputSource;
import org.apache.druid.data.input.impl.LocalInputSource;
import org.apache.druid.guice.annotations.UnstableApi;
import org.apache.druid.java.util.common.UOE;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.Set;

/**
 * InputSource abstracts the storage system where input data is stored. It creates an {@link InputSourceReader}
 * to read data from the given input source.
 * The most common use case would be:
 *
 * <pre>{@code
 *   InputSourceReader reader = inputSource.reader();
 *   try (CloseableIterator<InputRow> iterator = reader.read()) {
 *     while (iterator.hasNext()) {
 *       InputRow row = iterator.next();
 *       processRow(row);
 *     }
 *   }
 * }</pre>
 */
@UnstableApi
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = InputSource.TYPE_PROPERTY)
@JsonSubTypes(value = {
    @Type(name = LocalInputSource.TYPE_KEY, value = LocalInputSource.class),
    @Type(name = HttpInputSource.TYPE_KEY, value = HttpInputSource.class),
    @Type(name = InlineInputSource.TYPE_KEY, value = InlineInputSource.class),
    @Type(name = CombiningInputSource.TYPE_KEY, value = CombiningInputSource.class)
})
public interface InputSource
{
  String TYPE_PROPERTY = "type";

  /**
   * Returns true if this inputSource can be processed in parallel using ParallelIndexSupervisorTask. It must be
   * castable to SplittableInputSource and the various SplittableInputSource methods must work as documented.
   */
  @JsonIgnore
  boolean isSplittable();

  /**
   * Returns true if this inputSource supports different {@link InputFormat}s. Some inputSources such as
   * {@link LocalInputSource} can store files of any format. These storage types require an {@link InputFormat}
   * to be passed so that {@link InputSourceReader} can parse data properly. However, some storage types have
   * a fixed format. For example, druid inputSource always reads segments. These inputSources should return false for
   * this method.
   */
  boolean needsFormat();

  /**
   * Creates an {@link InputSourceReader}.
   *
   * @param inputRowSchema     for {@link InputRow}
   * @param inputFormat        to parse data. It can be null if {@link #needsFormat()} = true
   * @param temporaryDirectory to store temp data. It will be cleaned up automatically once the task is finished.
   */
  InputSourceReader reader(
      InputRowSchema inputRowSchema,
      @Nullable InputFormat inputFormat,
      File temporaryDirectory
  );

  /**
   * The types of input sources uses. A set is returned here, as some InputSource implementation allow for
   * combining of multiple input sources.
   * @return The types of input sources uses
   */
  @JsonIgnore
  @Nonnull
  default Set<String> getTypes()
  {
    throw new UOE("This inputSource does not support input source based security");
  }
}
