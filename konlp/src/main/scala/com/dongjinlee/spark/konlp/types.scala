/**
 * MIT License
 *
 * Copyright (c) 2019 Lee Dongjin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.dongjinlee.spark.konlp

import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object types {
  /**
   * 형태소 분석 결과. 아래와 같은 요소들을 가진다:
   *
   * - surface: 표면형.
   * - pos_id: 세종 품사표기 표준안 id.
   */
  val MorphemeType: StructType = StructType(
    StructField("surface", StringType, false) ::
      StructField("pos_id", IntegerType, false) ::
      Nil
  )
}
