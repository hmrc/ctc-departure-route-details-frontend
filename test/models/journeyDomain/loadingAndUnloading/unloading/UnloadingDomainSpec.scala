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

package models.journeyDomain.loadingAndUnloading.unloading

import base.SpecBase
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.loadingAndUnloading.unloading.*

class UnloadingDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val country  = arbitrary[Country].sample.value
  private val unlocode = arbitrary[String].sample.value

  "UnloadingDomain" - {

    "can be parsed from UserAnswers" - {
      "when add a place of unloading UN/LOCODE is yes and additional information is yes" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnLocodeYesNoPage, true)
          .setValue(UnLocodePage, unlocode)
          .setValue(AddExtraInformationYesNoPage, true)
          .setValue(CountryPage, country)
          .setValue(LocationPage, country.description)

        val expectedResult = UnloadingDomain(
          unLocode = Some(unlocode),
          additionalInformation = Some(AdditionalInformationDomain(country, country.description))
        )

        val result = UserAnswersReader[UnloadingDomain](
          UnloadingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          UnLocodeYesNoPage,
          UnLocodePage,
          AddExtraInformationYesNoPage,
          CountryPage,
          LocationPage
        )
      }

      "when add a place of unloading UN/LOCODE is yes and additional information is no" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnLocodeYesNoPage, true)
          .setValue(UnLocodePage, unlocode)
          .setValue(AddExtraInformationYesNoPage, false)

        val expectedResult = UnloadingDomain(
          unLocode = Some(unlocode),
          additionalInformation = None
        )

        val result = UserAnswersReader[UnloadingDomain](
          UnloadingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          UnLocodeYesNoPage,
          UnLocodePage,
          AddExtraInformationYesNoPage
        )
      }

      "when add a place of unloading UN/LOCODE is no" in {
        val userAnswers = emptyUserAnswers
          .setValue(UnLocodeYesNoPage, false)
          .setValue(CountryPage, country)
          .setValue(LocationPage, country.description)

        val expectedResult = UnloadingDomain(
          unLocode = None,
          additionalInformation = Some(AdditionalInformationDomain(country, country.description))
        )

        val result = UserAnswersReader[UnloadingDomain](
          UnloadingDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          UnLocodeYesNoPage,
          CountryPage,
          LocationPage
        )
      }
    }
  }
}
