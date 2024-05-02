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

package base

import config.{PostTransitionModule, TransitionModule}
import controllers.actions._
import models.{Index, LockCheck, Mode, SelectableList, UserAnswers}
import navigation._
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import repositories.SessionRepository
import services.{CountriesService, LockService}

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite with SpecBase =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository); reset(mockDataRetrievalActionProvider); reset(mockLockService)

    when(mockSessionRepository.set(any())(any())) thenReturn Future.successful(true)
    when(mockCountriesService.getCountryCodesCTC()(any())).thenReturn(Future.successful(SelectableList(Nil)))
    when(mockCountriesService.getCustomsSecurityAgreementAreaCountries()(any())).thenReturn(Future.successful(SelectableList(Nil)))
    when(mockLockService.checkLock(any())(any())).thenReturn(Future.successful(LockCheck.Unlocked))
  }

  final val mockSessionRepository: SessionRepository                     = mock[SessionRepository]
  final val mockDataRetrievalActionProvider: DataRetrievalActionProvider = mock[DataRetrievalActionProvider]
  final val mockCountriesService: CountriesService                       = mock[CountriesService]
  final val mockLockActionProvider: LockActionProvider                   = mock[LockActionProvider]
  final val mockLockService: LockService                                 = mock[LockService]

  final override def fakeApplication(): Application =
    guiceApplicationBuilder()
      .build()

  protected def setExistingUserAnswers(userAnswers: UserAnswers): Unit = setUserAnswers(Some(userAnswers))

  protected def setNoExistingUserAnswers(): Unit = setUserAnswers(None)

  private def setUserAnswers(userAnswers: Option[UserAnswers]): Unit = {
    when(mockLockActionProvider.apply()) thenReturn new FakeLockAction(mockLockService)
    when(mockDataRetrievalActionProvider.apply(any())) thenReturn new FakeDataRetrievalAction(userAnswers)
  }

  protected val onwardRoute: Call = Call("GET", "/foo")

  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  protected val fakeRouteDetailsNavigatorProvider: RouteDetailsNavigatorProvider =
    (mode: Mode) => new FakeRouteDetailsNavigator(onwardRoute, mode)

  protected val fakeExitNavigatorProvider: ExitNavigatorProvider =
    (mode: Mode) => new FakeExitNavigator(onwardRoute, mode)

  protected val fakeOfficeOfExitNavigatorProvider: OfficeOfExitNavigatorProvider =
    (mode: Mode, index: Index) => new FakeOfficeOfExitNavigator(onwardRoute, mode, index)

  protected val fakeLoadingNavigatorProvider: LoadingAndUnloadingNavigatorProvider =
    (mode: Mode) => new FakeLoadingAndUnloadingNavigator(onwardRoute, mode)

  protected val fakeLocationOfGoodsNavigatorProvider: LocationOfGoodsNavigatorProvider =
    (mode: Mode) => new FakeLocationOfGoodsNavigator(onwardRoute, mode)

  protected val fakeRoutingNavigatorProvider: RoutingNavigatorProvider =
    (mode: Mode) => new FakeRoutingNavigator(onwardRoute, mode)

  protected val fakeCountryOfRoutingNavigatorProvider: CountryOfRoutingNavigatorProvider =
    (mode: Mode, index: Index) => new FakeCountryOfRoutingNavigator(onwardRoute, mode, index)

  protected val fakeTransitNavigatorProvider: TransitNavigatorProvider =
    (mode: Mode) => new FakeTransitNavigator(onwardRoute, mode)

  protected val fakeOfficeOfTransitNavigatorProvider: OfficeOfTransitNavigatorProvider =
    (mode: Mode, index: Index) => new FakeOfficeOfTransitNavigator(onwardRoute, mode, index)

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
//        bind[DataRequiredAction].to[DataRequiredAction],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[LockActionProvider].toInstance(mockLockActionProvider),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRetrievalActionProvider].toInstance(mockDataRetrievalActionProvider),
        bind[DependentTasksAction].to[FakeDependentTasksAction],
        bind[CountriesService].toInstance(mockCountriesService),
        bind[LockService].toInstance(mockLockService)
      )

  protected def guiceApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

  protected def transitionApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()
      .disable[PostTransitionModule]
      .bindings(new TransitionModule)

  protected def postTransitionApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()
      .disable[TransitionModule]
      .bindings(new PostTransitionModule)
}
