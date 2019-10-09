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

import com.dongjin.spark.konlp.functions._
import com.dongjin.spark.konlp.types._
import com.holdenkarau.spark.testing.DataFrameSuiteBase
import kr.bydelta.koala.POS
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{StructField, StructType}
import org.scalatest.FunSuite

class FunctionsSuite extends FunSuite with DataFrameSuiteBase {

  import spark.implicits._

  def makeColumnsNullable(df: DataFrame): DataFrame = {
    val schema = StructType(df.schema.map(f => StructField(f.name, f.dataType, nullable = true)))
    df.sparkSession.createDataFrame(df.rdd, schema)
  }

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
    val expected = Seq(4, 5)
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
      .withColumn("morphemes", explode(morphemes(col("str"))))
      .withColumn("surface", col("morphemes.surface"))
      .withColumn("pos_id", col("morphemes.pos_id"))
      .select(col("surface"), col("pos_id"))
    val expected = Seq(
      ("바람", POS.NNG.ordinal),
      ("이", POS.JKS.ordinal),
      ("살짝", POS.MAG.ordinal),
      ("잠", POS.NNG.ordinal),
      ("을", POS.JKO.ordinal),
      ("깨우", POS.VV.ordinal),
      ("ㄴ", POS.ETM.ordinal),
      ("꽃잎", POS.NNP.ordinal),
      ("에", POS.JKB.ordinal),
      ("좋아하", POS.VV.ordinal),
      ("아", POS.EC.ordinal),
      ("이", POS.MM.ordinal),
      ("말", POS.NNG.ordinal),
      ("한마디", POS.NNG.ordinal),
      ("를", POS.JKO.ordinal),
      ("담", POS.VV.ordinal),
      ("아서", POS.EC.ordinal),
      ("어젯밤", POS.NNG.ordinal),
      ("꼬박", POS.MAG.ordinal),
      ("새우", POS.VV.ordinal),
      ("ㄴ", POS.ETM.ordinal),
      ("나", POS.NP.ordinal),
      ("의", POS.JKG.ordinal),
      ("노래", POS.NNG.ordinal),
      ("에", POS.JKB.ordinal),
      ("사랑", POS.NNG.ordinal),
      ("의", POS.JKG.ordinal),
      ("마법", POS.NNP.ordinal),
      ("을", POS.JKO.ordinal),
      ("걷", POS.VV.ordinal),
      ("어", POS.EC.ordinal),
      ("보", POS.VX.ordinal),
      ("네", POS.EC.ordinal)
    ).toDF("surface", "pos_id")
    assertDataFrameEquals(makeColumnsNullable(expected), makeColumnsNullable(actual))
  }
}
