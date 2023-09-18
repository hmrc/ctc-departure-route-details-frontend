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

package navigation

import base.SpecBase
import generators.Generators
import models.SecurityDetailsType.{EntryAndExitSummaryDeclarationSecurityDetails, EntrySummaryDeclarationSecurityDetails, NoSecurityDetails}
import models._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.SecurityDetailsTypePage

class LoadingAndUnloadingNavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LoadingAndUnloading Navigator" - {

    "when in NormalMode" - {

      val mode              = NormalMode
      val navigatorProvider = new LoadingAndUnloadingNavigatorProviderImpl()
      val navigator         = navigatorProvider.apply(mode)

      "when answers complete" - {
        "must redirect to route check your answers if security is not 0" in {

          val securityType   = Gen.oneOf(EntrySummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value
          val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

          forAll(arbitraryLoadingAndUnloadingAnswers(initialAnswers)) {
            answers =>
              navigator
                .nextPage(answers)
                .mustBe(controllers.loadingAndUnloading.routes.LoadingAndUnloadingAnswersController.onPageLoad(answers.lrn, mode))
          }
        }

        "must redirect to loading and unloading check your answers if security is 0" in {

          val initialAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, NoSecurityDetails)

          forAll(arbitraryLoadingAndUnloadingAnswers(initialAnswers)) {
            answers =>
              navigator
                .nextPage(answers)
                .mustBe(controllers.routes.RouteDetailsAnswersController.onPageLoad(answers.lrn))
          }
        }
      }
    }

    "when in CheckMode" - {

      val mode              = CheckMode
      val navigatorProvider = new LoadingAndUnloadingNavigatorProviderImpl()
      val navigator         = navigatorProvider.apply(mode)

      "when answers complete" - {
        "must redirect to route details check your answers" in {
          forAll(arbitraryRouteDetailsAnswers(emptyUserAnswers)) {
            answers =>
              navigator
                .nextPage(answers)
                .mustBe(controllers.routes.RouteDetailsAnswersController.onPageLoad(answers.lrn))
          }
        }
      }
    }
  }
}
