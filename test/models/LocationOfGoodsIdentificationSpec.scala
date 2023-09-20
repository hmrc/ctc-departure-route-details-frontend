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

package models

import base.SpecBase
import generators.Generators
import models.LocationOfGoodsIdentification._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.LocationTypePage
import play.api.libs.json.{JsError, JsString, Json}
import services.LocationOfGoodsIdentificationTypeService
import config.Constants._

class LocationOfGoodsIdentificationSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val allValues = goodsIdentificationValues

  "LocationOfGoodsIdentification" - {

    "must deserialise valid values" in {

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
            .as[LocationOfGoodsIdentification] mustBe locationOfGoodsIdentification
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!Seq(LocationOfGoodsIdentification(DesignatedLocation, "Designated location")).map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[LocationOfGoodsIdentification] mustEqual JsError("error.expected.jsobject")
      }
    }

    "must serialise" in {

      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (qualifier, description) =>
          val locationOfGoodsIdentification = LocationOfGoodsIdentification(qualifier, description)
          Json.toJson(locationOfGoodsIdentification) mustBe Json.parse(s"""
               |{
               |  "qualifier": "$qualifier",
               |  "description": "$description"
               |}
               |""".stripMargin)
      }
    }

    "valuesU" - {
      "when designated location (sub place)" - {
        "must return U and V" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType(DesignatedLocation, "Designated location"))

          LocationOfGoodsIdentificationTypeService.matchUserAnswers(userAnswers, allValues) mustBe Seq(
            LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier"),
            LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier")
          )
        }
      }

      "when authorised place (auth location code)" - {
        "must return Y" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType(AuthorisedPlace, "Authorised place"))

          LocationOfGoodsIdentificationTypeService.matchUserAnswers(userAnswers, allValues) mustBe Seq(
            LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
          )
        }
      }

      "when approved place (agreed location)" - {
        "must return  U, W, X, Y" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType(ApprovedPlace, "Approved place"))

          LocationOfGoodsIdentificationTypeService.matchUserAnswers(userAnswers, allValues) mustBe Seq(
            LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber"),
            LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier"),
            LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
            LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier")
          )
        }
      }

      "when other" - {
        "must return T, U, W, Z" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType(Other, "Other"))

          LocationOfGoodsIdentificationTypeService.matchUserAnswers(userAnswers, allValues) mustBe Seq(
            LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier"),
            LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier"),
            LocationOfGoodsIdentification(AddressIdentifier, "Address"),
            LocationOfGoodsIdentification(PostalCodeIdentifier, "PostalCode")
          )
        }
      }

      "when undefined location type" - {
        "must return all values" in {
          LocationOfGoodsIdentificationTypeService.matchUserAnswers(emptyUserAnswers, allValues) mustBe goodsIdentificationValues
        }
      }
    }
  }
}
