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

import base.SpecBase
import config.FrontendAppConfig
import generators.Generators
import models.reference.LocationOfGoodsIdentification.*
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}

class LocationOfGoodsIdentificationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val mockFrontendAppConfig = mock[FrontendAppConfig]

  "LocationOfGoodsIdentification" - {

    "must deserialise valid values" - {

      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (qualifier, description) =>
            val locationOfGoodsIdentification = LocationOfGoodsIdentification(qualifier, description)
            Json
              .parse(s"""
                   |{
                   |  "qualifier": "$qualifier",
                   |  "description": "$description"
                   |}
                   |""".stripMargin)
              .as[LocationOfGoodsIdentification] mustEqual locationOfGoodsIdentification
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(false)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (qualifier, description) =>
              val locationOfGoodsIdentification = LocationOfGoodsIdentification(qualifier, description)
              Json
                .parse(s"""
                         |{
                         |  "qualifier": "$qualifier",
                         |  "description": "$description"
                         |}
                         |""".stripMargin)
                .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(mockFrontendAppConfig)) mustEqual locationOfGoodsIdentification
          }
        }

        "when phase 6" in {
          when(mockFrontendAppConfig.isPhase6Enabled).thenReturn(true)
          forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
            (qualifier, description) =>
              val locationOfGoodsIdentification = LocationOfGoodsIdentification(qualifier, description)
              Json
                .parse(s"""
                         |{
                         |  "key": "$qualifier",
                         |  "value": "$description"
                         |}
                         |""".stripMargin)
                .as[LocationOfGoodsIdentification](LocationOfGoodsIdentification.reads(mockFrontendAppConfig)) mustEqual locationOfGoodsIdentification
          }
        }
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!goodsIdentificationValues.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[LocationOfGoodsIdentification] mustEqual JsError("error.expected.jsobject")
      }
    }

    "must serialise" in {

      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (qualifier, description) =>
          val locationOfGoodsIdentification = LocationOfGoodsIdentification(qualifier, description)
          Json.toJson(locationOfGoodsIdentification) mustEqual Json.parse(s"""
               |{
               |  "qualifier": "$qualifier",
               |  "description": "$description"
               |}
               |""".stripMargin)
      }
    }

  }
}
