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
package com.dongjin.spark.konlp

import org.apache.spark.sql.Column

object functions {
  /**
   * 주어진 문자열을 문장 단위로 분할한다.
   */
  def ssplit(str: Column): Column = new Column(SentenceSplit(str.expr))

  /**
   * 주어진 문자열을 단어 단위로 분할한다.
   */
  def wsplit(str: Column): Column = new Column(WordSplit(str.expr))

  /**
   * 주어진 문자열을 형태소 분석한다.
   */
  def morphemes(str: Column): Column = new Column(Morphemes(str.expr))

  /**
   * 주어진 문자열에 포함된 개체들을 리턴한다.
   */
  def entries(str: Column): Column = new Column(Entries(str.expr))
}
