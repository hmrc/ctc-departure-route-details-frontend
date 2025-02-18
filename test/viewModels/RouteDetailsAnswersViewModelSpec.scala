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

package viewModels

import base.{AppWithDefaultMockFixtures, SpecBase}
import generators.Generators
import models.CheckMode
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import viewModels.RouteDetailsAnswersViewModel.RouteDetailsAnswersViewModelProvider
import viewModels.exit.ExitAnswersViewModel
import viewModels.exit.ExitAnswersViewModel.ExitAnswersViewModelProvider
import viewModels.loadingAndUnloading.LoadingAndUnloadingAnswersViewModel
import viewModels.loadingAndUnloading.LoadingAndUnloadingAnswersViewModel.LoadingAndUnloadingAnswersViewModelProvider
import viewModels.locationOfGoods.LocationOfGoodsAnswersViewModel
import viewModels.locationOfGoods.LocationOfGoodsAnswersViewModel.LocationOfGoodsAnswersViewModelProvider
import viewModels.routing.RoutingAnswersViewModel
import viewModels.routing.RoutingAnswersViewModel.RoutingAnswersViewModelProvider
import viewModels.sections.Section
import viewModels.transit.TransitAnswersViewModel
import viewModels.transit.TransitAnswersViewModel.TransitAnswersViewModelProvider

class RouteDetailsAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val mockRoutingAnswersViewModelProvider             = mock[RoutingAnswersViewModelProvider]
  private val mockTransitAnswersViewModelProvider             = mock[TransitAnswersViewModelProvider]
  private val mockExitAnswersViewModelProvider                = mock[ExitAnswersViewModelProvider]
  private val mockLocationOfGoodsAnswersViewModelProvider     = mock[LocationOfGoodsAnswersViewModelProvider]
  private val mockLoadingAndUnloadingAnswersViewModelProvider = mock[LoadingAndUnloadingAnswersViewModelProvider]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockRoutingAnswersViewModelProvider)
    reset(mockTransitAnswersViewModelProvider)
    reset(mockExitAnswersViewModelProvider)
    reset(mockLocationOfGoodsAnswersViewModelProvider)
    reset(mockLoadingAndUnloadingAnswersViewModelProvider)
  }

  "apply" - {
    "must pass CheckMode to view models" in {
      def dummySection  = arbitrary[Section].sample.value
      def dummySections = arbitrary[List[Section]].sample.value

      val viewModelProvider = new RouteDetailsAnswersViewModelProvider(
        mockRoutingAnswersViewModelProvider,
        mockTransitAnswersViewModelProvider,
        mockExitAnswersViewModelProvider,
        mockLocationOfGoodsAnswersViewModelProvider,
        mockLoadingAndUnloadingAnswersViewModelProvider
      )

      forAll(arbitraryRouteDetailsAnswers(emptyUserAnswers)) {
        answers =>
          beforeEach()

          when(mockRoutingAnswersViewModelProvider.apply(any(), any())(any(), any())).thenReturn(RoutingAnswersViewModel(dummySections))
          when(mockTransitAnswersViewModelProvider.apply(any(), any())(any(), any())).thenReturn(TransitAnswersViewModel(dummySections))
          when(mockExitAnswersViewModelProvider.apply(any(), any())(any(), any())).thenReturn(ExitAnswersViewModel(dummySections))
          when(mockLocationOfGoodsAnswersViewModelProvider.apply(any(), any())(any(), any())).thenReturn(LocationOfGoodsAnswersViewModel(dummySection))
          when(mockLoadingAndUnloadingAnswersViewModelProvider.apply(any(), any())(any(), any())).thenReturn(LoadingAndUnloadingAnswersViewModel(dummySections))

          viewModelProvider.apply(answers)

          verify(mockRoutingAnswersViewModelProvider).apply(eqTo(answers), eqTo(CheckMode))(any(), any())
          verify(mockTransitAnswersViewModelProvider).apply(eqTo(answers), eqTo(CheckMode))(any(), any())
          verify(mockExitAnswersViewModelProvider).apply(eqTo(answers), eqTo(CheckMode))(any(), any())
          verify(mockLocationOfGoodsAnswersViewModelProvider).apply(eqTo(answers), eqTo(CheckMode))(any(), any())
          verify(mockLoadingAndUnloadingAnswersViewModelProvider).apply(eqTo(answers), eqTo(CheckMode))(any(), any())
      }
    }
  }
}
