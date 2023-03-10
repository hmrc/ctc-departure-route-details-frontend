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

import models.journeyDomain.RouteDetailsDomain
import models.journeyDomain.exit.{ExitDomain, OfficeOfExitDomain}
import models.journeyDomain.loadingAndUnloading.LoadingAndUnloadingDomain
import models.journeyDomain.loadingAndUnloading.loading.LoadingDomain
import models.journeyDomain.loadingAndUnloading.unloading.UnloadingDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.routing.{CountryOfRoutingDomain, RoutingDomain}
import models.journeyDomain.transit.{OfficeOfTransitDomain, TransitDomain}
import models.{Index, UserAnswers}
import org.scalacheck.Gen

trait RouteDetailsUserAnswersGenerator {
  self: UserAnswersGenerator =>

  def arbitraryRouteDetailsAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[RouteDetailsDomain](userAnswers)(
      RouteDetailsDomain.userAnswersReader(ctcCountryCodes, customsSecurityAgreementAreaCountryCodes)
    )

  def arbitraryRoutingAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[RoutingDomain](userAnswers)

  def arbitraryCountryOfRoutingAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[CountryOfRoutingDomain](userAnswers)(CountryOfRoutingDomain.userAnswersReader(index))

  def arbitraryTransitAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[TransitDomain](userAnswers)(
      TransitDomain.userAnswersReader(ctcCountryCodes, customsSecurityAgreementAreaCountryCodes)
    )

  def arbitraryOfficeOfTransitAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[OfficeOfTransitDomain](userAnswers)(
      OfficeOfTransitDomain.userAnswersReader(index, ctcCountryCodes, customsSecurityAgreementAreaCountryCodes)
    )

  def arbitraryExitAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[ExitDomain](userAnswers)

  def arbitraryOfficeOfExitAnswers(userAnswers: UserAnswers, index: Index): Gen[UserAnswers] =
    buildUserAnswers[OfficeOfExitDomain](userAnswers)(
      OfficeOfExitDomain.userAnswersReader(index)
    )

  def arbitraryLocationOfGoodsAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LocationOfGoodsDomain](userAnswers)

  def arbitraryLoadingAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LoadingDomain](userAnswers)

  def arbitraryUnloadingAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[UnloadingDomain](userAnswers)

  def arbitraryLoadingAndUnloadingAnswers(userAnswers: UserAnswers): Gen[UserAnswers] =
    buildUserAnswers[LoadingAndUnloadingDomain](userAnswers)
}
