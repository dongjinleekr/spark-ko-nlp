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

import com.dongjin.spark.konlp.types.MorphemeType
import kr.bydelta.koala.POS
import org.apache.spark.sql.Column
import org.apache.spark.sql.functions.{array_contains, lit}

import scala.collection.JavaConverters._

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
   * 주어진 형태소 분석 결과가 체언인지를 판별한다.
   */
  def isNoun(morpheme: Column): Column =
    hasTagOf(morpheme, lit(POS.getNOUNS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 용언인지를 판별한다.
   */
  def isPredicate(morpheme: Column): Column =
    hasTagOf(morpheme, lit(POS.getPREDICATES.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 수식언인지를 판별한다.
   */
  def isModifier(morpheme: Column): Column =
    hasTagOf(morpheme, lit(POS.getMODIFIERS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 관계언인지를 판별한다.
   */
  def isPostPosition(morpheme: Column): Column =
  hasTagOf(morpheme, lit(POS.getPOSTPOSITIONS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 어미인지를 판별한다.
   */
  def isEnding(morpheme: Column): Column =
  hasTagOf(morpheme, lit(POS.getENDINGS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 접사인지를 판별한다.
   */
  def isAffix(morpheme: Column): Column =
  hasTagOf(morpheme, lit(POS.getAFFIXES.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 기호인지를 판별한다.
   */
  def isSymbol(morpheme: Column): Column =
  hasTagOf(morpheme, lit(POS.getSYMBOLS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 미확인 품사인지를 판별한다.
   */
  def isUnknown(morpheme: Column): Column =
  hasTagOf(morpheme, lit(POS.getUNKNOWNS.asScala.map(_.ordinal).toArray))

  /**
   * 주어진 형태소 분석 결과가 주어진 형태소 유형 집합에 포함되는지를 판별한다.
   */
  def hasTagOf(morpheme: Column, tags: Column): Column =
    array_contains(tags, morpheme.cast(MorphemeType).apply("pos_id"))
}
