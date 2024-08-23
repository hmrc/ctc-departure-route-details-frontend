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

package pages.sections.transit

import controllers.transit.routes
import models.journeyDomain._
import models.{Index, Mode, UserAnswers}
import pages.sections.Section
import pages.transit.index.OfficeOfTransitInCL147Page
import play.api.libs.json.{JsArray, JsPath}
import play.api.mvc.Call

case object OfficesOfTransitSection extends Section[JsArray] {

  override def path: JsPath = TransitSection.path \ toString

  override def toString: String = "officesOfTransit"

  override def route(userAnswers: UserAnswers, mode: Mode): Option[Call] =
    Some(routes.AddAnotherOfficeOfTransitController.onPageLoad(userAnswers.lrn, mode))

  def atLeastOneOfficeOfTransitIsNotInCL147: Read[Boolean] =
    this.arrayReader.apply(_).map(_.to(_.value.length)).flatMap {
      case ReaderSuccess(numberOfOfficesOfTransit, pages) =>
        (0 until numberOfOfficesOfTransit)
          .foldLeft(UserAnswersReader.success(false)) {
            (acc, index) =>
              RichTuple2(
                (acc, OfficeOfTransitInCL147Page(Index(index)).reader)
              ).to {
                case (areAnyOfficesOfTransitNotInCL147, isThisOfficeOfTransitInCL147) =>
                  pages =>
                    val result = areAnyOfficesOfTransitNotInCL147 || !isThisOfficeOfTransitInCL147
                    ReaderSuccess(result, pages).toUserAnswersReader
              }
          }
          .apply(pages)
    }
}
