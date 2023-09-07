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
import models.LocationOfGoodsIdentification._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.LocationTypePage
import play.api.libs.json.{JsError, JsString, Json}

class LocationOfGoodsIdentificationSpec extends SpecBase with ScalaCheckPropertyChecks {

  "LocationOfGoodsIdentification" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(LocationOfGoodsIdentification.values)

      forAll(gen) {
        locationOfGoodsIdentification =>
          JsString(locationOfGoodsIdentification.toString).validate[LocationOfGoodsIdentification].asOpt.value mustEqual locationOfGoodsIdentification
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!LocationOfGoodsIdentification.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>
          JsString(invalidValue).validate[LocationOfGoodsIdentification] mustEqual JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(LocationOfGoodsIdentification.values)

      forAll(gen) {
        locationOfGoodsIdentification =>
          Json.toJson(locationOfGoodsIdentification) mustEqual JsString(locationOfGoodsIdentification.toString)
      }
    }

    "valuesU" - {
      "when designated location (sub place)" - {
        "must return U and V" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType("A", "Designated location"))

          LocationOfGoodsIdentification.values(userAnswers) mustBe Seq(
            CustomsOfficeIdentifier,
            UnlocodeIdentifier
          )
        }
      }

      "when authorised place (auth location code)" - {
        "must return Y" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType("A", "Authorised place"))

          LocationOfGoodsIdentification.values(userAnswers) mustBe Seq(
            AuthorisationNumber
          )
        }
      }

      "when approved place (agreed location)" - {
        "must return T, U, W, X, Z" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType("A", "Approved place"))

          LocationOfGoodsIdentification.values(userAnswers) mustBe Seq(
            EoriNumber,
            CoordinatesIdentifier,
            UnlocodeIdentifier,
            AddressIdentifier,
            PostalCode
          )
        }
      }

      "when other" - {
        "must return T, U, W, Z" in {
          val userAnswers = emptyUserAnswers.setValue(LocationTypePage, LocationType("A", "Other"))

          LocationOfGoodsIdentification.values(userAnswers) mustBe Seq(
            CoordinatesIdentifier,
            UnlocodeIdentifier,
            AddressIdentifier,
            PostalCode
          )
        }
      }

      "when undefined location type" - {
        "must return all values" in {
          LocationOfGoodsIdentification.values(emptyUserAnswers) mustBe Seq(
            CustomsOfficeIdentifier,
            EoriNumber,
            AuthorisationNumber,
            CoordinatesIdentifier,
            UnlocodeIdentifier,
            AddressIdentifier,
            PostalCode
          )
        }
      }
    }
  }
}
