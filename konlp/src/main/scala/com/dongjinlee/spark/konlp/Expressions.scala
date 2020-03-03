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

import types._
import kr.bydelta.koala.proc.CanTag
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.catalyst.expressions.codegen.CodegenFallback
import org.apache.spark.sql.catalyst.expressions.{Expression, ImplicitCastInputTypes}
import org.apache.spark.sql.catalyst.util.GenericArrayData
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

import scala.collection.JavaConverters._


private[konlp] object Expressions {
  private val SUPPORTED_TAGGER_CLASSES = Seq(
    // 아리랑
    "kr.bydelta.koala.arirang.Tagger",
    //
    "kr.bydelta.koala.daon.Tagger",
    // 은전 한 닢
    "kr.bydelta.koala.eunjeon.Tagger",
    // 코모란
    "kr.bydelta.koala.kmr.Tagger",
    // rhino
    "kr.bydelta.koala.rhino.Tagger")

  lazy val DETECTED_TAGGER_CLASSES = SUPPORTED_TAGGER_CLASSES.flatMap { className =>
    try {
      Some(Class.forName(className).asInstanceOf[Class[CanTag]])
    } catch {
      case _: ClassNotFoundException => None
    }
  }

  require(DETECTED_TAGGER_CLASSES.size == 1, s"Multiple Tagger implementations in classpath: $DETECTED_TAGGER_CLASSES")

  val tagger: CanTag = DETECTED_TAGGER_CLASSES(0).newInstance
}

private[konlp] case class SentenceSplit(expr: Expression)
  extends Expression with ImplicitCastInputTypes with CodegenFallback {

  override def nullable: Boolean = false

  override def dataType: DataType =
    ArrayType(StringType, containsNull = false)

  override def inputTypes: Seq[DataType] = Seq(StringType)

  override def children: Seq[Expression] = expr :: Nil

  override def eval(input: InternalRow): Any = {
    val str = expr.eval(input)
    if (str == null) {
      null
    } else {
      val value = str.asInstanceOf[UTF8String].toString
      val sentences = Expressions.tagger.tag(value).asScala.map { sentence =>
        UTF8String.fromString(sentence.surfaceString)
      }
      new GenericArrayData(sentences)
    }
  }

  override def prettyName: String = "ssplit"
}

private[konlp] case class WordSplit(expr: Expression)
  extends Expression with ImplicitCastInputTypes with CodegenFallback {

  override def nullable: Boolean = false

  override def dataType: DataType =
    ArrayType(StringType, containsNull = false)

  override def inputTypes: Seq[DataType] = Seq(StringType)

  override def children: Seq[Expression] = expr :: Nil

  override def eval(input: InternalRow): Any = {
    val str = expr.eval(input)
    if (str == null) {
      null
    } else {
      val value = str.asInstanceOf[UTF8String].toString
      val words = Expressions.tagger.tag(value).asScala.flatMap { sentence =>
        sentence.asScala.map { word =>
          UTF8String.fromString(word.getSurface)
        }
      }
      new GenericArrayData(words)
    }
  }

  override def prettyName: String = "wsplit"
}

private[konlp] case class Morphemes(expr: Expression)
  extends Expression with ImplicitCastInputTypes with CodegenFallback {

  override def nullable: Boolean = false

  override def dataType: DataType =
    ArrayType(MorphemeType, containsNull = false)

  override def inputTypes: Seq[DataType] = Seq(StringType)

  override def children: Seq[Expression] = expr :: Nil

  override def eval(input: InternalRow): Any = {
    val str = expr.eval(input)
    if (str == null) {
      null
    } else {
      val value = str.asInstanceOf[UTF8String].toString
      val morphemes = Expressions.tagger.tag(value).asScala.flatMap { sentence =>
        sentence.asScala.flatMap { word =>
          word.asScala.map { morpheme =>
            InternalRow(UTF8String.fromString(morpheme.getSurface), morpheme.getTag.ordinal)
          }
        }
      }
      new GenericArrayData(morphemes)
    }
  }

  override def prettyName: String = "morphemes"
}
