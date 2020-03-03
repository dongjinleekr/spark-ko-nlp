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

import functions._
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import kr.bydelta.koala.POS
import kr.bydelta.koala.proc.CanTag
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalatest.FunSuite

import scala.collection.JavaConverters._
import scala.language.implicitConversions

trait FunctionsSuite extends FunSuite with DataFrameSuiteBase {

  def tagger: CanTag

  def makeColumnsNullable(df: DataFrame): DataFrame = {
    val schema = StructType(df.schema.map(f => StructField(f.name, f.dataType, nullable = true)))
    df.sparkSession.createDataFrame(df.rdd, schema)
  }

  def toSentences(str: String): Seq[String] = {
    tagger.tag(str).asScala.map(_.surfaceString)
  }

  def toWords(str: String): Seq[String] = {
    tagger.tag(str).asScala.flatMap { sentence =>
      sentence.asScala.map(_.getSurface)
    }
  }

  def toMorphemes(str: String): Seq[(String, Int)] = {
    tagger.tag(str).asScala.flatMap { sentence =>
      sentence.asScala.flatMap { word =>
        word.asScala.map { morpheme =>
          (morpheme.getSurface, morpheme.getTag.ordinal)
        }
      }
    }
  }

  import spark.implicits._

  test("ssplit splits into sentences") {
    val text =
      """불안한 사람들은 새로운 적을 찾아 헤맨다.
        |어디로 가는가 얼만큼 왔는가 혹은 제자리인가.
        |거꾸로 가는가 알기는 아는가 이게 뭔 소린가.""".stripMargin.replaceAll("\\n", " ")
    val df = Seq(text).toDF("str")
    val actual = df
      .withColumn("sentence", explode(ssplit(col("str"))))
      .select(col("sentence"))
    assert(actual.collect.map(_.toSeq).flatten.size == 3)
  }

  test("wsplit splits into words") {
    val text =
      """나의 바다야, 나의 하늘아.
        |나를 안고서 그렇게 잠들면 돼.""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("wordCount", size(wsplit(col("str"))))
      .select(col("wordCount"))

    val expected = text
      .map(str => toWords(str).size)
      .toDF("wordCount")
    assertDataFrameEquals(expected, actual)
  }

  test("morphemes analyzes POS") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("surface", col("morpheme.surface"))
      .withColumn("pos_id", col("morpheme.pos_id"))
      .select(col("surface"), col("pos_id"))

    val expected = text.flatMap(toMorphemes(_)).toDF("surface", "pos_id")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isNoun") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isNoun", isNoun(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isNoun"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isNoun = POS.getNOUNS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isNoun)
      }.toDF("surface", "isNoun")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isPredicate") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isPredicate", isPredicate(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isPredicate"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isPredicate = POS.getPREDICATES.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isPredicate)
      }.toDF("surface", "isPredicate")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isModifier") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isModifier", isModifier(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isModifier"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isModifier = POS.getMODIFIERS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isModifier)
      }.toDF("surface", "isModifier")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isPostPosition") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isPostPosition", isPostPosition(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isPostPosition"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isPostPosition = POS.getPOSTPOSITIONS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isPostPosition)
      }.toDF("surface", "isPostPosition")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isEnding") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isEnding", isEnding(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isEnding"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isEnding = POS.getENDINGS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isEnding)
      }.toDF("surface", "isEnding")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isAffix") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isAffix", isAffix(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isAffix"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isAffix = POS.getAFFIXES.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isAffix)
      }.toDF("surface", "isAffix")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isSymbol") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isSymbol", isSymbol(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isSymbol"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isSymbol = POS.getSYMBOLS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isSymbol)
      }.toDF("surface", "isSymbol")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test isUnknown") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isUnknown", isUnknown(col("morpheme")))
      .select(col("morpheme.surface").as("surface"), col("isUnknown"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isUnknown = POS.getUNKNOWNS.asScala.map(_.ordinal).contains(morpheme._2)

        (surface, isUnknown)
      }.toDF("surface", "isUnknown")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }

  test("test hasTagOf") {
    val text =
      """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq
    val df = text.toDF("str")
    val actual = df
      .withColumn("morpheme", explode(morphemes(col("str"))))
      .withColumn("isNoun",
        hasTagOf(col("morpheme"), lit(POS.getNOUNS.asScala.map(_.ordinal).toArray)))
      .withColumn("isPredicate",
        hasTagOf(col("morpheme"), lit(POS.getPREDICATES.asScala.map(_.ordinal).toArray)))
      .withColumn("isNounOrPredicate",
        hasTagOf(col("morpheme"),
          lit((POS.getNOUNS.asScala ++ POS.getPREDICATES.asScala).map(_.ordinal).toArray)))
      .select(col("morpheme.surface").as("surface"),
        col("isNoun"), col("isPredicate"), col("isNounOrPredicate"))

    val expected = text.flatMap(toMorphemes(_))
      .map { morpheme =>
        val surface = morpheme._1
        val isNoun = POS.getNOUNS.asScala.map(_.ordinal).contains(morpheme._2)
        val isPredicate = POS.getPREDICATES.asScala.map(_.ordinal).contains(morpheme._2)
        val isNounOrPredicate = (POS.getNOUNS.asScala ++ POS.getPREDICATES.asScala)
          .map(_.ordinal).contains(morpheme._2)

        (surface, isNoun, isPredicate, isNounOrPredicate)
      }.toDF("surface", "isNoun", "isPredicate", "isNounOrPredicate")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }
}
