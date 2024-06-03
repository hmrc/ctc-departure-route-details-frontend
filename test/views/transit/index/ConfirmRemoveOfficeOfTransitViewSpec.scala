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

package views.transit.index

import forms.YesNoFormProvider
import generators.Generators
import models.Mode
import org.scalacheck.Arbitrary.arbitrary
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.transit.RemoveOfficeOfTransitViewModel
import views.behaviours.YesNoViewBehaviours
import views.html.transit.index.ConfirmRemoveOfficeOfTransitView

class ConfirmRemoveOfficeOfTransitViewSpec extends YesNoViewBehaviours with Generators {

  private lazy val officeOfTransit = arbitraryCustomsOffice.arbitrary.sample.value

  private val mode = arbitrary[Mode].sample.value

  private val viewModel = new RemoveOfficeOfTransitViewModel(Some(officeOfTransit))

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector
      .instanceOf[ConfirmRemoveOfficeOfTransitView]
      .apply(form, lrn, mode, index, viewModel, viewModel.officeName)(fakeRequest, messages)

  override val prefix: String = "transit.index.confirmRemoveOfficeOfTransit"

  behave like pageWithTitle(officeOfTransit.name)

  behave like pageWithBackLink()

  behave like pageWithSectionCaption("Route details - Office of transit")

  behave like pageWithHeading(officeOfTransit.name)

  behave like pageWithRadioItems(args = Seq(officeOfTransit.name))

  behave like pageWithSubmitButton("Save and continue")

  "when no office name is present in user answers" - {
    val viewModel = new RemoveOfficeOfTransitViewModel(None)
    val form      = new YesNoFormProvider()(prefix)
    val view = injector
      .instanceOf[ConfirmRemoveOfficeOfTransitView]
      .apply(form, lrn, mode, index, viewModel, viewModel.officeName)(fakeRequest, messages)
    val doc = parseView(view)

    behave like pageWithTitle(doc, prefix)

    behave like pageWithHeading(doc, prefix)
  }
}
