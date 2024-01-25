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

package models.journeyDomain.loadingAndUnloading.loading

import base.SpecBase
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.loadingAndUnloading.loading._

class AdditionalInformationDomainSpec extends SpecBase with Generators {

  "AdditionalInformation" - {

    "can be parsed from UserAnswers" - {

      "when additional information has a country" in {
        val country1      = arbitrary[Country].sample.value
        val loadingPlace1 = Gen.alphaNumStr.sample.value.take(35)

        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, country1)
          .setValue(LocationPage, loadingPlace1)

        val expectedResult = AdditionalInformationDomain(
          country = country1,
          location = loadingPlace1
        )

        val result = UserAnswersReader[AdditionalInformationDomain](
          AdditionalInformationDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          CountryPage,
          LocationPage
        )
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when additional information has no country" in {

        val placeOfLoading = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationPage, placeOfLoading)

        val result = UserAnswersReader[AdditionalInformationDomain](
          AdditionalInformationDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe CountryPage
        result.left.value.pages mustBe Seq(
          CountryPage
        )
      }

      "when additional information has no place of loading" in {
        val country1 = arbitrary[Country].sample.value

        val userAnswers = emptyUserAnswers
          .setValue(CountryPage, country1)

        val result = UserAnswersReader[AdditionalInformationDomain](
          AdditionalInformationDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe LocationPage
        result.left.value.pages mustBe Seq(
          CountryPage,
          LocationPage
        )
      }
    }
  }
}
