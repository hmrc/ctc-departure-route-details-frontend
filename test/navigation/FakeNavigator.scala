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

import config.FrontendAppConfig
import models.{CountryList, Index, Mode, UserAnswers}
import play.api.mvc.Call

class FakeNavigator(desiredRoute: Call) extends Navigator {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeRouteDetailsNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig)
    extends RouteDetailsNavigator(mode, CountryList(Nil), CountryList(Nil)) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeExitNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends ExitNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeOfficeOfExitNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig) extends OfficeOfExitNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeLoadingAndUnloadingNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends LoadingAndUnloadingNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeLocationOfGoodsNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends LocationOfGoodsNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeRoutingNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig) extends RoutingNavigator(mode) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeCountryOfRoutingNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig)
    extends CountryOfRoutingNavigator(mode, index) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeTransitNavigator(desiredRoute: Call, mode: Mode)(implicit config: FrontendAppConfig)
    extends TransitNavigator(mode, CountryList(Nil), CountryList(Nil)) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}

class FakeOfficeOfTransitNavigator(desiredRoute: Call, mode: Mode, index: Index)(implicit config: FrontendAppConfig)
    extends OfficeOfTransitNavigator(mode, index, CountryList(Nil), CountryList(Nil)) {
  override def nextPage(userAnswers: UserAnswers): Call = desiredRoute
}
