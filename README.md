spark-ko-nlp
=====

`spark-ko-nlp`는 Apache Spark를 위한 한국어 자연어 처리 패키지입니다. [KoalaNLP](https://koalanlp.github.io/koalanlp/)에 기반하여 형태소 분석기에 독립적인 일관적이고 표준적인 인터페이스를 제공하며, Spark의 User Defined Function 기능을 사용하지 않음으로써 효율적인 처리를 구현합니다.

# 빠른 시작

```scala
import com.dongjinlee.spark.konlp.functions._
import com.dongjinlee.spark.konlp.types._
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
 |-- pos_id: integer (nullable = false) // 형태소 id
```

## Function

### ssplit

주어진 문자열을 문장 단위로 분할합니다.

|       |     input    |          output         |
|:-----:|:------------:|:-----------------------:|
| param |    문자열    |     분할된 문장 집합    |
|  type | `StringType` | `ArrayType(StringType)` |

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

주어진 문자열을 단어 단위로 분할합니다.

|       |     input    |          output         |
|:-----:|:------------:|:-----------------------:|
| param |    문자열    |     분할된 단어 집합    |
|  type | `StringType` | `ArrayType(StringType)` |

### morphemes

주어진 문자열을 형태소 단위로 분할합니다.

|       |     input    |           output          |
|:-----:|:------------:|:-------------------------:|
| param |    문자열    |     포함된 형태소 집합    |
|  type | `StringType` | `ArrayType(MorphemeType)` |

### isNoun

형태소 분석 결과가 체언(명사, 수사, 대명사)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |   체언 여부   |
|  type |  `MorphemeType`  | `BooleanType` |

### isPredicate

형태소 분석 결과가 용언(동사, 형용사)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |   용언 여부   |
|  type |  `MorphemeType`  | `BooleanType` |

### isModifier

형태소 분석 결과가 수식언(관형사, 부사)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |  수식언 여부  |
|  type |  `MorphemeType`  | `BooleanType` |

### isPostPosition

형태소 분석 결과가 관계언(조사)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |  관계언 여부  |
|  type |  `MorphemeType`  | `BooleanType` |

### isEnding

형태소 분석 결과가 어미(선어말 어미, 종결 어미, ...)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |   어미 여부   |
|  type |  `MorphemeType`  | `BooleanType` |

### isAffix

형태소 분석 결과가 접사(체언 접두사, 용언 접두사, ...)인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |   접사 여부   |
|  type |  `MorphemeType`  | `BooleanType` |

### isSymbol

주어진 형태소 id 값 혹은 형태소 분석 결과가 기호인지를 판별합니다.

|       |       input      |     output    |
|:-----:|:----------------:|:-------------:|
| param | 형태소 분석 결과 |   기호 여부   |
|  type |  `MorphemeType`  | `BooleanType` |

### isUnknown

형태소 분석 결과가 미확인 품사인지를 판별합니다.

|       |       input      |      output      |
|:-----:|:----------------:|:----------------:|
| param | 형태소 분석 결과 | 미확인 품사 여부 |
|  type |  `MorphemeType`  |   `BooleanType`  |

### hasTagOf

형태소 분석 결과가 주어진 형태소 id 집합에 포함되는지를 판별합니다.

|       |      input1      |          input2          |     output    |
|:-----:|:----------------:|:------------------------:|:-------------:|
| param | 형태소 분석 결과 |      형태소 id 배열      |   포함 여부   |
|  type |  `MorphemeType`  | `ArrayType(IntegerType)` | `BooleanType` |

# How to Build

프로젝트를 빌드하기 위해서는 sbt가 필요합니다.

```
sbt clean       # 빌드된 파일 제거
sbt compile     # 컴파일
sbt test        # 테스트
sbt package     # artifact 생성
```

\* 모든 기능 및 테스트는 konlp에 구현되어 있습니다. arirang, daon, kmr, rhino 모듈에는 지원되는 형태소 분석기에 대한 테스트를 수행하는 코드만 있습니다.

# 기타

## Dependency 설정

spark-ko-nlp를 사용하려면 [KoalaNLP](https://koalanlp.github.io/koalanlp/)에 호환되는 형태소 분석 모듈을 추가해 주어야 합니다. 현재 아래와 같은 모듈들을 지원하며, dependency 설정만 해 주면 추가 설정 필요 없이 자동으로 인식됩니다:

|    이름    |   groupId  |    artifactId    |
|:----------:|:----------:|:----------------:|
|   아리랑   | kr.bydelta | koalanlp-arirang |
|    daon    | kr.bydelta |   koalanlp-daon  |
|   코모란   | kr.bydelta |   koalanlp-kmr   |
|    rhino   | kr.bydelta |  koalanlp-rhino  |

버전은 koala-nlp와 동일한 버전을 써 주시면 됩니다. 버전 호환성은 아래와 같습니다:

| spark-ko-nlp | koala-nlp |    scala   | spark |
|:------------:|:---------:|:----------:|:-----:|
|      0.5     |   2.0.5   | 2.11, 2.12 | 2.4.x |

## 세종 품사표기 표준안

scala-ko-nlp는 KoalaNLP의 세종 품사표기 표준안 정의를 따릅니다. ([참고](http://koalanlp.github.io/koalanlp/api/koalanlp/kr.bydelta.koala/-p-o-s/index.html))

| id | code |  분류  |       한글 명칭      |
|----|:----:|:------:|:--------------------:|
| 0  |  NNG |  체언  |       일반명사       |
| 1  |  NNP |  체언  |       고유명사       |
| 2  |  NNB |  체언  |     일반 의존명사    |
| 3  |  NNM |  체언  |    단위성 의존명사   |
| 4  |  NR  |  체언  |         수사         |
| 5  |  NP  |  체언  |        대명사        |
| 6  |  VV  |  용언  |         동사         |
| 7  |  VA  |  용언  |        형용사        |
| 8  |  VX  |  용언  |       보조용언       |
| 9  |  VCP |  용언  |      긍정지정사      |
| 10 |  VCN |  용언  |      부정지정사      |
| 11 |  MM  | 수식언 |        관형사        |
| 12 |  MAG | 수식언 |         부사         |
| 13 |  MAJ | 수식언 |       접속부사       |
| 14 |  IC  | 독립언 |        감탄사        |
| 15 |  JKS | 관계언 |       주격 조사      |
| 16 |  JKC | 관계언 |       보격 조사      |
| 17 |  JKG | 관계언 |      관형격 조사     |
| 18 |  JKO | 관계언 |      목적격 조사     |
| 19 |  JKB | 관계언 |      부사격 조사     |
| 20 |  JKV | 관계언 |       호격 조사      |
| 21 |  JKQ | 관계언 |      인용격 조사     |
| 22 |  JC  | 관계언 |       접속 조사      |
| 23 |  JX  | 관계언 |        보조사        |
| 24 |  EP  |  어미  |      선어말 어미     |
| 25 |  EF  |  어미  |       종결 어미      |
| 26 |  EC  |  어미  |       연결 어미      |
| 27 |  ETN |  어미  |    명사형 전성어미   |
| 28 |  ETM |  어미  |    관형형 전성어미   |
| 29 |  XPN |  접사  |      체언 접두사     |
| 30 |  XPV |  접사  |      용언 접두사     |
| 31 |  XSN |  접사  |   명사 파생 접미사   |
| 32 |  XSV |  접사  |   동사 파생 접미사   |
| 33 |  XSA |  접사  |  형용사 파생 접미사  |
| 34 |  XSM |  접사  |   부사 파생 접미사   |
| 35 |  XSO |  접사  |      기타 접미사     |
| 36 |  XR  |  접사  |         어근         |
| 37 |  SF  |  기호  |       종결기호       |
| 38 |  SP  |  기호  |       연결기호       |
| 39 |  SS  |  기호  |       묶음기호       |
| 40 |  SE  |  기호  |       생략기호       |
| 41 |  SO  |  기호  |       붙임기호       |
| 42 |  SW  |  기호  |       기타기호       |
| 43 |  NF  |        |    명사 추정 범주    |
| 44 |  NV  |        |    동사 추정 범주    |
| 45 |  NA  |        |    분석 불능 범주    |
| 46 |  SL  |        |        외국어        |
| 47 |  SH  |        |         한자         |
| 48 |  SN  |        |         숫자         |
| 49 | TEMP |        | 임시기호(내부처리용) |
