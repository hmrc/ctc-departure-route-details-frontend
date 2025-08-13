/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.reference

import config.FrontendAppConfig
import generators.Generators
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json

class LocationTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues with Generators {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "LocationType" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val locationType = LocationType(code, description)
          Json.toJson(locationType) mustEqual Json.parse(s"""
            |{
            |  "type": "$code",
            |  "description": "$description"
            |}
            |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val locationType = LocationType(code, description)
            Json
              .parse(s"""
                   |{
                   |  "type": "$code",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[LocationType] mustEqual locationType
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(false)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, description) =>
              val locationType = LocationType(code, description)
              Json
                .parse(s"""
                         |{
                         |  "type": "$code",
                         |  "description": "$description"
                         |}
                         |""".stripMargin)
                .as[LocationType](LocationType.reads(mockFrontendAppConfig)) mustEqual locationType
          }
        }

        "when phase 6" in {
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(true)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (code, description) =>
              val locationType = LocationType(code, description)
              Json
                .parse(s"""
                         |{
                         |  "key": "$code",
                         |  "value": "$description"
                         |}
                         |""".stripMargin)
                .as[LocationType](LocationType.reads(mockFrontendAppConfig)) mustEqual locationType
          }
        }
      }
    }

    "must format as string" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val locationType = LocationType(code, description)
          locationType.toString mustEqual s"$description"
      }
    }
  }
}
