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

package models.journeyDomain.loadingAndUnloading

import base.SpecBase
import generators.Generators
import models.SecurityDetailsType._
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage
import pages.loadingAndUnloading.AddPlaceOfUnloadingPage

class LoadingAndUnloadingDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloadingDomain" - {

    "unloadingReader" - {
      "can be parsed from UserAnswers" - {
        "when SecurityType is in Set{1, 3}" in {
          val securityType   = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
          val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

          forAll(arbitraryUnloadingAnswers(initialAnswers)) {
            answers =>
              val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                LoadingAndUnloadingDomain.unloadingReader
              ).run(answers)

              result.value mustBe defined
          }
        }

        "when SecurityType is in Set{0}" in {
          val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, NoSecurityDetails)

          val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
            LoadingAndUnloadingDomain.unloadingReader
          ).run(initialAnswers)

          result.value must not be defined
        }

        "when SecurityType is in Set{2}" - {
          "And adding a place of unloading" in {
            val initialAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, ExitSummaryDeclarationSecurityDetails)
              .setValue(AddPlaceOfUnloadingPage, true)

            forAll(arbitraryUnloadingAnswers(initialAnswers)) {
              answers =>
                val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                  LoadingAndUnloadingDomain.unloadingReader
                ).run(answers)

                result.value mustBe defined
            }
          }

          "And not adding a place of unloading" in {
            val initialAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, ExitSummaryDeclarationSecurityDetails)
              .setValue(AddPlaceOfUnloadingPage, false)

            val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
              LoadingAndUnloadingDomain.unloadingReader
            ).run(initialAnswers)

            result.value must not be defined
          }

        }

      }
    }

  }
}
