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

package models.journeyDomain.locationOfGoods

import base.SpecBase
import generators.Generators
import models.journeyDomain.UserAnswersReader
import org.scalacheck.Gen
import pages.locationOfGoods.contact._

class AdditionalContactDomainSpec extends SpecBase with Generators {

  "AdditionalContact" - {

    "can be parsed from UserAnswers" - {

      "when additional contact has a name and telephone number" in {
        val name            = Gen.alphaNumStr.sample.value
        val telephoneNumber = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, name)
          .setValue(TelephoneNumberPage, telephoneNumber)

        val expectedResult = AdditionalContactDomain(
          name = name,
          telephoneNumber = telephoneNumber
        )

        val result = UserAnswersReader[AdditionalContactDomain](
          AdditionalContactDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          NamePage,
          TelephoneNumberPage
        )
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when additional contact has no name" in {

        val telephoneNumber = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(TelephoneNumberPage, telephoneNumber)

        val result = UserAnswersReader[AdditionalContactDomain](
          AdditionalContactDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe NamePage
        result.left.value.pages mustBe Seq(
          NamePage
        )
      }

      "when additional contact has no telephone number" in {
        val name = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(NamePage, name)

        val result = UserAnswersReader[AdditionalContactDomain](
          AdditionalContactDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe TelephoneNumberPage
        result.left.value.pages mustBe Seq(
          NamePage,
          TelephoneNumberPage
        )
      }
    }
  }
}
