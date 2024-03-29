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
import config.Constants.AdditionalDeclarationType._
import config.Constants.SecurityType._
import config.PhaseConfig
import generators.Generators
import models.Phase
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.reference.SpecificCircumstanceIndicator
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.SpecificCircumstanceIndicatorPage
import pages.external.{AdditionalDeclarationTypePage, SecurityDetailsTypePage}
import pages.loadingAndUnloading.{loading, unloading, AddPlaceOfLoadingYesNoPage, AddPlaceOfUnloadingPage}
import pages.sections.LoadingAndUnloadingSection

class LoadingAndUnloadingDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloadingDomain" - {

    "userAnswersReader" - {
      "when during transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

        "and security is 0" - {
          "then loading and unloading are skipped" in {
            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)

            val result = UserAnswersReader[LoadingAndUnloadingDomain](
              LoadingAndUnloadingDomain.userAnswersReader(mockPhaseConfig).apply(Nil)
            ).run(userAnswers)

            result.value.value mustBe LoadingAndUnloadingDomain(None, None)
            result.value.pages mustBe Nil
          }
        }

        "and security is not 0" - {
          "then loading and unloading are skipped" in {
            val security  = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value
            val unlocode1 = Gen.alphaNumStr.sample.value
            val unlocode2 = Gen.alphaNumStr.sample.value

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, security)
              .setValue(loading.AddUnLocodeYesNoPage, true)
              .setValue(loading.UnLocodePage, unlocode1)
              .setValue(loading.AddExtraInformationYesNoPage, false)
              .setValue(unloading.UnLocodeYesNoPage, true)
              .setValue(unloading.UnLocodePage, unlocode2)
              .setValue(unloading.AddExtraInformationYesNoPage, false)

            val result = UserAnswersReader[LoadingAndUnloadingDomain](
              LoadingAndUnloadingDomain.userAnswersReader(mockPhaseConfig).apply(Nil)
            ).run(userAnswers)

            result.value.value mustBe LoadingAndUnloadingDomain(
              loading = Some(
                LoadingDomain(
                  unLocode = Some(unlocode1),
                  additionalInformation = None
                )
              ),
              unloading = Some(
                UnloadingDomain(
                  unLocode = Some(unlocode2),
                  additionalInformation = None
                )
              )
            )
            result.value.pages mustBe Seq(
              loading.AddUnLocodeYesNoPage,
              loading.UnLocodePage,
              loading.AddExtraInformationYesNoPage,
              unloading.UnLocodeYesNoPage,
              unloading.UnLocodePage,
              unloading.AddExtraInformationYesNoPage,
              LoadingAndUnloadingSection
            )
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
                val result = UserAnswersReader[Option[UnloadingDomain]](
                  LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
                ).run(answers)

                result.value.value mustBe defined
            }
          }

          "when SecurityType is in Set{0}" in {
            val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, NoSecurityDetails)

            val result = UserAnswersReader[Option[UnloadingDomain]](
              LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
            ).run(initialAnswers)

            result.value.value must not be defined
          }

          "when SecurityType is in Set{2}" - {
            "And adding a place of unloading" in {
              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, ExitSummaryDeclarationSecurityDetails)
                .setValue(AddPlaceOfUnloadingPage, true)

              forAll(arbitraryUnloadingAnswers(initialAnswers)) {
                answers =>
                  val result = UserAnswersReader[Option[UnloadingDomain]](
                    LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
                  ).run(answers)

                  result.value.value mustBe defined
              }
            }

            "And not adding a place of unloading" in {
              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, ExitSummaryDeclarationSecurityDetails)
                .setValue(AddPlaceOfUnloadingPage, false)

              val result = UserAnswersReader[Option[UnloadingDomain]](
                LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
              ).run(initialAnswers)

              result.value.value must not be defined
            }
          }
        }
      }

      "cannot be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and security is not 0" - {

            "and specific circumstance indicator is XXX" - {
              "and add place of unloading is unanswered" in {

                val securityType = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value

                forAll(arbitrary[SpecificCircumstanceIndicator](arbitraryXXXSpecificCircumstanceIndicator), securityType) {
                  (specificCircumstanceIndicator, security) =>
                    val userAnswers = emptyUserAnswers
                      .setValue(SecurityDetailsTypePage, security)
                      .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

                    val result = UserAnswersReader[Option[UnloadingDomain]](
                      LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
                    ).run(userAnswers)

                    result.left.value.page mustBe AddPlaceOfUnloadingPage
                    result.left.value.pages mustBe Seq(
                      AddPlaceOfUnloadingPage
                    )
                }
              }
            }

            "and specific circumstance indicator is not XXX or undefined" - {
              "and add unloading UN/LOCODE is unanswered" in {
                forAll(arbitrary[String](arbitrarySomeSecurityDetailsType), Gen.option(arbitrary[SpecificCircumstanceIndicator])) {
                  (securityType, specificCircumstanceIndicator) =>
                    val userAnswers = emptyUserAnswers
                      .setValue(SecurityDetailsTypePage, securityType)
                      .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

                    val result = UserAnswersReader[Option[UnloadingDomain]](
                      LoadingAndUnloadingDomain.unloadingReader(mockPhaseConfig).apply(Nil)
                    ).run(userAnswers)

                    result.left.value.page mustBe unloading.UnLocodeYesNoPage
                    result.left.value.pages mustBe Seq(
                      unloading.UnLocodeYesNoPage
                    )
                }
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

            val result = UserAnswersReader[Option[LoadingDomain]](
              LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
            ).run(userAnswers)

            result.value.value mustBe None
            result.value.pages mustBe Nil
          }
        }

        "when post transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "and not pre-lodge" in {
            val userAnswers = emptyUserAnswers.setValue(AdditionalDeclarationTypePage, Standard)
            forAll(arbitraryLoadingAnswers(userAnswers)) {
              answers =>
                val result = UserAnswersReader[Option[LoadingDomain]](
                  LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
                ).run(answers)

                result.value.value mustBe defined
            }
          }

          "and pre-lodge" - {
            "when addPlaceOfLoading is yes" in {
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalDeclarationTypePage, PreLodge)
                .setValue(AddPlaceOfLoadingYesNoPage, true)

              forAll(arbitraryLoadingAnswers(userAnswers)) {
                answers =>
                  val result = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
                  ).run(answers)

                  result.value.value mustBe defined
              }
            }
            "when addPlaceOfLoading is no" in {
              val userAnswers = emptyUserAnswers
                .setValue(AdditionalDeclarationTypePage, PreLodge)
                .setValue(AddPlaceOfLoadingYesNoPage, false)
              forAll(arbitraryLoadingAnswers(userAnswers)) {
                answers =>
                  val result = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
                  ).run(answers)

                  result.value.value must not be defined
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
              forAll(arbitrary[String](arbitrarySomeSecurityDetailsType)) {
                security =>
                  val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, security)

                  val result = UserAnswersReader[Option[LoadingDomain]](
                    LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
                  ).run(userAnswers)

                  result.left.value.page mustBe loading.AddUnLocodeYesNoPage
                  result.left.value.pages mustBe Seq(
                    loading.AddUnLocodeYesNoPage
                  )
              }
            }
          }
        }

        "when post-transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and add place of loading UN/LOCODE yes/no is unanswered" in {
            forAll(arbitrary[String](arbitrarySomeSecurityDetailsType)) {
              security =>
                val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, security)

                val result = UserAnswersReader[Option[LoadingDomain]](
                  LoadingAndUnloadingDomain.loadingReader(mockPhaseConfig).apply(Nil)
                ).run(userAnswers)

                result.left.value.page mustBe loading.AddUnLocodeYesNoPage
                result.left.value.pages mustBe Seq(
                  loading.AddUnLocodeYesNoPage
                )
            }
          }
        }
      }
    }
  }
}
