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
import config.{Constants, PhaseConfig}
import generators.Generators
import models.SecurityDetailsType._
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.reference.SpecificCircumstanceIndicator
import models.{Phase, SecurityDetailsType}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.SpecificCircumstanceIndicatorPage
import pages.external.{AdditionalDeclarationTypePage, SecurityDetailsTypePage}
import pages.loadingAndUnloading.{loading, unloading, AddPlaceOfLoadingYesNoPage, AddPlaceOfUnloadingPage}

class LoadingAndUnloadingDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloadingDomain" - {

    "userAnswersReader" - {
      "when during transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "and security is 0" - {
          "and add country and location yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, SecurityDetailsType.NoSecurityDetails)

            val result: EitherType[LoadingAndUnloadingDomain] = UserAnswersReader[LoadingAndUnloadingDomain](
              LoadingAndUnloadingDomain.userAnswersReader(mockPhaseConfig)
            ).run(userAnswers)

            result.left.value.page mustBe unloading.AddExtraInformationYesNoPage
          }
        }
      }
    }

    "unloadingReader" - {
      "can be parsed from UserAnswers" - {
        "when post transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "when SecurityType is in Set{1, 3}" in {
            val securityType   = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
            val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

            forAll(arbitraryUnloadingAnswers(initialAnswers)) {
              answers =>
                val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                  LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
                ).run(answers)

                result.value mustBe defined
            }
          }

          "when SecurityType is in Set{0}" in {
            val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, NoSecurityDetails)

            val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
              LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
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
                    LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
                  ).run(answers)

                  result.value mustBe defined
              }
            }

            "And not adding a place of unloading" in {
              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, ExitSummaryDeclarationSecurityDetails)
                .setValue(AddPlaceOfUnloadingPage, false)

              val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
              ).run(initialAnswers)

              result.value must not be defined
            }
          }
        }
      }

      "cannot be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and specific circumstance indicator is XXX" - {
            "and add place of unloading is unanswered" in {
              forAll(arbitrary[SpecificCircumstanceIndicator](arbitraryXXXSpecificCircumstanceIndicator)) {
                specificCircumstanceIndicator =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

                  val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                    LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
                  ).run(userAnswers)

                  result.left.value.page mustBe AddPlaceOfUnloadingPage
              }
            }
          }

          "and specific circumstance indicator is not XXX or undefined" - {
            "and add unloading UN/LOCODE is unanswered" in {
              forAll(arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType), Gen.option(arbitrary[SpecificCircumstanceIndicator])) {
                (securityType, specificCircumstanceIndicator) =>
                  val userAnswers = emptyUserAnswers
                    .setValue(SecurityDetailsTypePage, securityType)
                    .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

                  val result: EitherType[Option[UnloadingDomain]] = UserAnswersReader[Option[UnloadingDomain]](
                    LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig)
                  ).run(userAnswers)

                  result.left.value.page mustBe unloading.UnLocodeYesNoPage
              }
            }
          }
        }
      }
    }

    "loadingReader" - {
      "can be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and no security" in {
            val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, NoSecurityDetails)

            val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
              LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
            ).run(userAnswers)

            result.value mustBe None
          }
        }

        "when post transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "and not pre-lodge" in {
            val userAnswers = emptyUserAnswers.setValue(AdditionalDeclarationTypePage, Constants.STANDARD)
            forAll(arbitraryLoadingAnswers(userAnswers)) {
              answers =>
                val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
                  LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
                ).run(answers)

                result.value mustBe defined
            }
          }

          "and pre-lodge" - {
            "when addPlaceOfLoading is yes" in {
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalDeclarationTypePage, Constants.`PRE-LODGE`)
                .setValue(AddPlaceOfLoadingYesNoPage, true)

              forAll(arbitraryLoadingAnswers(userAnswers)) {
                answers =>
                  val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
                  ).run(answers)

                  result.value mustBe defined
              }
            }
            "when addPlaceOfLoading is no" in {
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalDeclarationTypePage, Constants.`PRE-LODGE`)
                .setValue(AddPlaceOfLoadingYesNoPage, false)
              forAll(arbitraryLoadingAnswers(userAnswers)) {
                answers =>
                  val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
                  ).run(answers)

                  result.value must not be defined
              }
            }
          }
        }
      }

      "cannot be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and security is not 0" - {
            "and add place of loading UN/LOCODE yes/no is unanswered" in {
              forAll(arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)) {
                security =>
                  val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, security)

                  val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
                  ).run(userAnswers)

                  result.left.value.page mustBe loading.AddUnLocodeYesNoPage
              }
            }
          }
        }

        "when post-transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and add place of loading UN/LOCODE yes/no is unanswered" in {
            forAll(arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)) {
              security =>
                val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, security)

                val result: EitherType[Option[LoadingDomain]] = UserAnswersReader[Option[LoadingDomain]](
                  LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig)
                ).run(userAnswers)

                result.left.value.page mustBe loading.AddUnLocodeYesNoPage
            }
          }
        }
      }
    }
  }
}
