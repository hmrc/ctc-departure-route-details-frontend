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
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.transit.TransitDomain
import models.{CheckMode, Mode, NormalMode}

import javax.inject.{Inject, Singleton}

@Singleton
class TransitNavigatorProviderImpl @Inject() (implicit config: FrontendAppConfig) extends TransitNavigatorProvider {

  def apply(mode: Mode): UserAnswersNavigator =
    mode match {
      case NormalMode =>
        new TransitNavigator(mode)
      case CheckMode =>
        new RouteDetailsNavigator(mode)
    }
}

trait TransitNavigatorProvider {

  def apply(mode: Mode): UserAnswersNavigator
}

class TransitNavigator(
  override val mode: Mode
)(implicit override val config: FrontendAppConfig)
    extends UserAnswersNavigator {

  override type T = TransitDomain

  implicit override val reader: UserAnswersReader[TransitDomain] =
    TransitDomain.userAnswersReader.apply(Nil)
}
