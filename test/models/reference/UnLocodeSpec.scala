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
import cats.data.NonEmptySet
import config.FrontendAppConfig
import generators.Generators
import models.SelectableList
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.govukfrontend.views.viewmodels.select.SelectItem

class UnLocodeSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "UnLocode" - {

    "must serialise" in {
      forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
        (code, description) =>
          val unLocode = UnLocode(code, description)
          Json.toJson(unLocode) mustEqual Json.parse(s"""
              |{
              |  "unLocodeExtendedCode": "$code",
              |  "name": "$description"
              |}
              |""".stripMargin)
      }
    }

    "must deserialise" - {
      "when reading from mongo" in {
        forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
          (code, description) =>
            val unLocode = UnLocode(code, description)
            Json
              .parse(s"""
                   |{
                   |  "unLocodeExtendedCode": "$code",
                   |  "name": "$description"
                   |}
                   |""".stripMargin)
              .as[UnLocode] mustEqual unLocode
        }
      }

      "when reading from reference data" - {
        "when phase 5" in {
          running(_.configure("feature-flags.phase-6-enabled" -> false)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val unLocode = UnLocode(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "unLocodeExtendedCode": "$code",
                         |  "name": "$description"
                         |}
                         |""".stripMargin)
                    .as[UnLocode](UnLocode.reads(config)) mustEqual unLocode
              }
          }
        }

        "when phase 6" in {
          running(_.configure("feature-flags.phase-6-enabled" -> true)) {
            app =>
              val config = app.injector.instanceOf[FrontendAppConfig]
              forAll(Gen.alphaNumStr, Gen.alphaNumStr) {
                (code, description) =>
                  val unLocode = UnLocode(code, description)
                  Json
                    .parse(s"""
                         |{
                         |  "key": "$code",
                         |  "value": "$description"
                         |}
                         |""".stripMargin)
                    .as[UnLocode](UnLocode.reads(config)) mustEqual unLocode
              }
          }
        }
      }
    }

    "must convert to select item" in {
      forAll(arbitrary[UnLocode], arbitrary[Boolean]) {
        (unLocode, selected) =>
          unLocode.toSelectItem(selected) mustEqual SelectItem(
            Some(unLocode.unLocodeExtendedCode),
            s"${unLocode.name} (${unLocode.unLocodeExtendedCode})",
            selected
          )
      }
    }

    "must format as string" in {
      forAll(arbitrary[UnLocode]) {
        unLocode =>
          unLocode.toString mustEqual s"${unLocode.name} (${unLocode.unLocodeExtendedCode})"
      }
    }

    "must order" in {
      val unLocode1 = UnLocode("FRTY4", "Thauvenay")
      val unLocode2 = UnLocode("ZWZVS", "Zvishavane")
      val unLocode3 = UnLocode("ADALV", "Andorra la Vella")

      val unLocodes = NonEmptySet.of(unLocode1, unLocode2, unLocode3)

      val result = SelectableList(unLocodes).values

      result mustEqual Seq(
        unLocode3,
        unLocode1,
        unLocode2
      )
    }
  }

}
