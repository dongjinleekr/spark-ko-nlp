spark-ko-nlp
=====

`spark-ko-nlp`는 Apache Spark를 위한 한국어 자연어 처리 패키지입니다. 쉽게 이야기하자면 **[`spark-corenlp`](https://github.com/databricks/spark-corenlp)의 한국어판**이라고 할 수 있습니다.

# 빠른 시작

```scala
import com.dongjin.spark.konlp.functions._
import com.dongjin.spark.konlp.types._
import org.apache.spark.sql.functions._

...

// 입력 문자열
val lines = """바람이 살짝 잠을 깨운 꽃잎에
        |좋아해 이 말 한마디를 담아서
        |어젯밤 꼬박 새운 나의 노래에
        |사랑의 마법을 걸어보네""".stripMargin.split('\n').toSeq

// 형태소 분석
val df = lines.toDF("str")
  .withColumn("morphemes", explode(morphemes(col("str"))))
  .withColumn("surface", col("morphemes.surface"))
  .withColumn("pos_id", col("morphemes.pos_id"))
  .select(col("surface"), col("pos_id"))

df.show

// +-------+------+
// |surface|pos_id|
// +-------+------+
// |  바람  |   0  |
// |   이  |  15  |
// |  살짝  |  12  |
// |   잠  |   0  |
// |   을  |  18  |
// |  깨우  |   6  |
// |   ㄴ  |  28  |
// ...
// +-------+------+
```

# 제공 기능

이 패키지는 Spark SQL에 아래와 같은 Type과 Function을 추가합니다.

## Type

- `MorphemeType`: 형태소 분석 결과입니다. 아래와 같은 schema를 가집니다:

```
root
 |-- surface: string (nullable = false) // 표면형
 |-- pos_id: integer (nullable = false) // 세종 품사표기 표준안 id
```

## Function

### ssplit

주어진 문자열을 문장 단위로 분할합니다. (`StringType` => `ArrayType(StringType)`)

```scala
// 입력 문자열
val text = """13인의 아해가 도로로 질주하오.
       |길은 막다른 골목이 적당하오.
       |
       |제 1의 아해가 무섭다고 그리오.""".stripMargin.replaceAll("\\n", " ")

// 문장 단위 분할
val df = Seq(text).toDF("str")
  .withColumn("sentence", explode(ssplit(col("str"))))
  .select(col("sentence"))

df.printSchema

// root
//  |-- sentence: string (nullable = false)s 8s

df.show

// +------------------------------+
// |           sentence           |
// +------------------------------+
// |   13인의 아해가 도로로 질주하오.  |
// |    길은 막다른 골목이 적당하오.   |
// |   제 1의 아해가 무섭다고 그리오.  |
// +------------------------------+
```

### wsplit

주어진 문자열을 단어 단위로 분할합니다. (`StringType` => `ArrayType(StringType)`)

```scala
```

### morphemes

주어진 문자열을 형태소 단위로 분할합니다. (`StringType` => `MorphemeType`)

```scala
```

### 기타: 현재 작업중 (v0.5)

- entries: (`StringType` => `ArrayType(StringType)`)
- isNoun: 주어진 `pos_id`값 혹은 `MorphemeType`이 명사인지를 판별합니다. (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)
- isPredicate: (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)
- isModifier: (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)
- isJosa: (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)
- hasTag: (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)
- hasTagOneOf: (`IntegerType` => `BooleanType`, `MorphemeType` => `BooleanType`)

# 기타

## 버전 호환성

| spark-ko-nlp | koala-nlp |    scala   |          비고         |
|:------------:|:---------:|:----------:|:---------------------:|
|      0.5     |   2.0.5   | 2.11, 2.12 |  Spark 2.4.3에서 작업 |

## 세종 품사표기 표준안

http://koalanlp.github.io/koalanlp/api/koalanlp/kr.bydelta.koala/-p-o-s/index.html

아래와 같습니다. [참고](https://github.com/koalanlp/koalanlp/blob/master/core/src/main/kotlin/kr/bydelta/koala/types.kt)

| id | code | 한글 명칭 |
|:---|:----:|:---------:|
|  0 |  NNG |    체언 (일반명사)   |
|  1 |  NNP |    명사   |
|  2 |  NNB |    명사   |
