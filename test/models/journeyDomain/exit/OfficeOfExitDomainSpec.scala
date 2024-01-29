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

package models.journeyDomain.exit

import base.SpecBase
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.exit.index._
import pages.sections.exit.OfficeOfExitSection

class OfficeOfExitDomainSpec extends SpecBase with Generators {

  "OfficeOfExitDomain" - {

    val country       = arbitrary[Country].sample.value
    val customsOffice = arbitrary[CustomsOffice].sample.value

    "can be parsed from UserAnswers" - {

      "when country and office answered at given index" - {
        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfExitCountryPage(index), country)
          .setValue(OfficeOfExitPage(index), customsOffice)

        val expectedResult = OfficeOfExitDomain(
          country = country,
          customsOffice = customsOffice
        )(index)

        val result = UserAnswersReader[OfficeOfExitDomain](
          OfficeOfExitDomain.userAnswersReader(index).apply(Nil)
        ).run(userAnswers)

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          OfficeOfExitCountryPage(index),
          OfficeOfExitPage(index),
          OfficeOfExitSection(index)
        )
      }
    }

    "cannot be parsed from user answers" - {
      "when country missing" in {
        val userAnswers = emptyUserAnswers

        val result = UserAnswersReader[OfficeOfExitDomain](
          OfficeOfExitDomain.userAnswersReader(index).apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe OfficeOfExitCountryPage(index)
        result.left.value.pages mustBe Seq(
          OfficeOfExitCountryPage(index)
        )
      }

      "when office missing" - {
        "and country defined" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfExitCountryPage(index), country)

          val result = UserAnswersReader[OfficeOfExitDomain](
            OfficeOfExitDomain.userAnswersReader(index).apply(Nil)
          ).run(userAnswers)

          result.left.value.page mustBe OfficeOfExitPage(index)
          result.left.value.pages mustBe Seq(
            OfficeOfExitCountryPage(index),
            OfficeOfExitPage(index)
          )
        }
      }
    }
  }
}
