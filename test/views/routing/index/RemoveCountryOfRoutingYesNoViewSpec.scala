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

package views.routing.index

import generators.Generators
import models.Mode
import models.reference.Country
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.routing.index.RemoveCountryOfRoutingYesNoView

class RemoveCountryOfRoutingYesNoViewSpec extends YesNoViewBehaviours with Generators {

  private val country: Country = arbitrary[Country].sample.value

  private val mode = arbitrary[Mode].sample.value

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[RemoveCountryOfRoutingYesNoView].apply(form, lrn, mode, index, country)(fakeRequest, messages)

  override val prefix: String = "routing.index.removeCountryOfRoutingYesNo"

  behave like pageWithTitle(country.description)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Transit route")

  behave like pageWithHeading(country.description)

  behave like pageWithRadioItems(args = Seq(country.description))

  behave like pageWithSubmitButton("Save and continue")
}
