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
import models.Index
import models.journeyDomain.UserAnswersReader
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import pages.exit.AddAnotherOfficeOfExitPage
import pages.exit.index.*
import pages.sections.exit.OfficeOfExitSection

class ExitDomainSpec extends SpecBase with Generators {

  "ExitDomain" - {

    val country       = arbitrary[Country].sample.value
    val customsOffice = arbitrary[CustomsOffice].sample.value

    "can be parsed from UserAnswers" - {

      "when at least one office of exit" in {
        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfExitCountryPage(index), country)
          .setValue(OfficeOfExitPage(index), customsOffice)

        val expectedResult = ExitDomain(
          officesOfExit = OfficesOfExitDomain(
            Seq(
              OfficeOfExitDomain(
                country = country,
                customsOffice = customsOffice
              )(index)
            )
          )
        )

        val result = UserAnswersReader[ExitDomain](
          ExitDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.value.value mustEqual expectedResult
        result.value.pages mustEqual Seq(
          OfficeOfExitCountryPage(index),
          OfficeOfExitPage(index),
          OfficeOfExitSection(index),
          AddAnotherOfficeOfExitPage
        )
      }
    }

    "cannot be parsed from user answers" - {
      "when no offices of exit" in {
        val userAnswers = emptyUserAnswers

        val result = UserAnswersReader[ExitDomain](
          ExitDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustEqual OfficeOfExitCountryPage(Index(0))
        result.left.value.pages mustEqual Seq(
          OfficeOfExitCountryPage(Index(0))
        )
      }
    }
  }
}
