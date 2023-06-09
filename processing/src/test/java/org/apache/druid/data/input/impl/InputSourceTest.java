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

package org.apache.druid.data.input.impl;

import org.apache.druid.data.input.InputFormat;
import org.apache.druid.data.input.InputRowSchema;
import org.apache.druid.data.input.InputSource;
import org.apache.druid.data.input.InputSourceReader;
import org.apache.druid.java.util.common.UOE;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;

public class InputSourceTest
{
  private static InputSource INPUT_SOURCE = new InputSource()
  {
    @Override
    public boolean isSplittable()
    {
      return false;
    }

    @Override
    public boolean needsFormat()
    {
      return false;
    }

    @Override
    public InputSourceReader reader(
        InputRowSchema inputRowSchema,
        @Nullable InputFormat inputFormat,
        File temporaryDirectory
    )
    {
      return null;
    }
  };

  @Test
  public void testGetTypes()
  {
    Assert.assertThrows(UOE.class, () -> INPUT_SOURCE.getTypes());
  }
}
