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

import config.{FrontendAppConfig, PhaseConfig}
import models.domain.UserAnswersReader
import models.journeyDomain.exit.ExitDomain
import models.{CheckMode, Mode, NormalMode}

import javax.inject.{Inject, Singleton}

@Singleton
class ExitNavigatorProviderImpl @Inject() (implicit config: FrontendAppConfig, phaseConfig: PhaseConfig) extends ExitNavigatorProvider {

  def apply(mode: Mode): UserAnswersNavigator =
    mode match {
      case NormalMode =>
        new ExitNavigator(mode)
      case CheckMode =>
        new RouteDetailsNavigator(mode)
    }
}

trait ExitNavigatorProvider {

  def apply(mode: Mode): UserAnswersNavigator
}

class ExitNavigator(override val mode: Mode)(implicit override val config: FrontendAppConfig, implicit override val phaseConfig: PhaseConfig)
    extends UserAnswersNavigator {

  override type T = ExitDomain

  implicit override val reader: UserAnswersReader[ExitDomain] =
    ExitDomain.userAnswersReader
}
