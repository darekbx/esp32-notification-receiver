package com.rpifilebrowser.di.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.cosmose.m5stickwidget.viewmodel.BLEViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {

    @Binds
    internal abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelFactory.ViewModelKey(BLEViewModel::class)
    internal abstract fun bindBLEViewModel(viewModel: BLEViewModel): ViewModel
}