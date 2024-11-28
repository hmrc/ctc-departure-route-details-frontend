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

package generators

import config.PhaseConfig
import models.journeyDomain.RouteDetailsDomain
import models.journeyDomain.exit.{ExitDomain, OfficeOfExitDomain}
import models.journeyDomain.loadingAndUnloading.LoadingAndUnloadingDomain
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.routing.{CountryOfRoutingDomain, RoutingDomain}
import models.journeyDomain.transit.{OfficeOfTransitDomain, OfficesOfTransitDomain, TransitDomain}
import models.{Index, UserAnswers}
import org.scalacheck.Gen

trait RouteDetailsUserAnswersGenerator {
  self: UserAnswersGenerator =>

  def arbitraryRouteDetailsAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[RouteDetailsDomain](userAnswers)

  def arbitraryRoutingAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[RoutingDomain](userAnswers)(
      RoutingDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryCountryOfRoutingAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[CountryOfRoutingDomain](userAnswers)(
      CountryOfRoutingDomain.userAnswersReader(index).apply(Nil)
    )

  def arbitraryTransitAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[TransitDomain](userAnswers)(
      TransitDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryOfficesOfTransitAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[OfficesOfTransitDomain](userAnswers)(
      OfficesOfTransitDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryOfficeOfTransitAnswers(userAnswers: UserAnswers, index: Index)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[OfficeOfTransitDomain](userAnswers)(
      OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
    )

  def arbitraryExitAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[ExitDomain](userAnswers)(
      ExitDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryOfficeOfExitAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[OfficeOfExitDomain](userAnswers)(
      OfficeOfExitDomain.userAnswersReader(index).apply(Nil)
    )

  def arbitraryLocationOfGoodsAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LocationOfGoodsDomain](userAnswers)(
      LocationOfGoodsDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryLoadingAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LoadingDomain](userAnswers)(
      LoadingDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryUnloadingAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[UnloadingDomain](userAnswers)(
      UnloadingDomain.userAnswersReader.apply(Nil)
    )

  def arbitraryLoadingAndUnloadingAnswers(userAnswers: UserAnswers)(implicit phaseConfig: PhaseConfig): Gen[UserAnswers] =
    buildUserAnswers[LoadingAndUnloadingDomain](userAnswers)(
      LoadingAndUnloadingDomain.userAnswersReader.apply(Nil)
    )
}
