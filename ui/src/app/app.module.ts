import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

import { AppComponent } from './app.component';
import { RecipeListComponent } from './components/recipe-list/recipe-list.component';
import { RecipeFormComponent } from './components/recipe-form/recipe-form.component';
import { BatchRunListComponent } from './components/batch-run-list/batch-run-list.component';
import { BatchRunFormComponent } from './components/batch-run-form/batch-run-form.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { AppRoutingModule } from './app-routing.module';
import { NavigationComponent } from './components/navigation/navigation.component';
import { PLCStatusComponent } from './components/plc-status/plc-status.component';
import { BatchMonitorComponent } from './components/batch-monitor/batch-monitor.component';

@NgModule({
  declarations: [
    AppComponent,
    RecipeListComponent,
    RecipeFormComponent,
    BatchRunListComponent,
    BatchRunFormComponent,
    DashboardComponent,
    NavigationComponent,
    PLCStatusComponent,
    BatchMonitorComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    CommonModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
