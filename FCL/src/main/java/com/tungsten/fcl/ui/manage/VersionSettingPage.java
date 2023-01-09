package com.tungsten.fcl.ui.manage;

import android.content.Context;
import android.view.View;
import android.widget.ArrayAdapter;

import com.tungsten.fcl.R;
import com.tungsten.fcl.game.FCLGameRepository;
import com.tungsten.fcl.setting.Profile;
import com.tungsten.fcl.setting.VersionSetting;
import com.tungsten.fcl.util.AndroidUtils;
import com.tungsten.fcl.util.FXUtils;
import com.tungsten.fcl.util.WeakListenerHolder;
import com.tungsten.fclauncher.FCLConfig;
import com.tungsten.fclcore.fakefx.beans.InvalidationListener;
import com.tungsten.fclcore.fakefx.beans.binding.Bindings;
import com.tungsten.fclcore.fakefx.beans.property.BooleanProperty;
import com.tungsten.fclcore.fakefx.beans.property.IntegerProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleBooleanProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleIntegerProperty;
import com.tungsten.fclcore.fakefx.beans.property.SimpleStringProperty;
import com.tungsten.fclcore.fakefx.beans.property.StringProperty;
import com.tungsten.fclcore.game.JavaVersion;
import com.tungsten.fclcore.game.ProcessPriority;
import com.tungsten.fclcore.task.Schedulers;
import com.tungsten.fclcore.task.Task;
import com.tungsten.fclcore.util.Lang;
import com.tungsten.fclcore.util.platform.MemoryUtils;
import com.tungsten.fcllibrary.component.ui.FCLCommonPage;
import com.tungsten.fcllibrary.component.view.FCLCheckBox;
import com.tungsten.fcllibrary.component.view.FCLEditText;
import com.tungsten.fcllibrary.component.view.FCLImageButton;
import com.tungsten.fcllibrary.component.view.FCLImageView;
import com.tungsten.fcllibrary.component.view.FCLLinearLayout;
import com.tungsten.fcllibrary.component.view.FCLProgressBar;
import com.tungsten.fcllibrary.component.view.FCLSeekBar;
import com.tungsten.fcllibrary.component.view.FCLSpinner;
import com.tungsten.fcllibrary.component.view.FCLSwitch;
import com.tungsten.fcllibrary.component.view.FCLTextView;
import com.tungsten.fcllibrary.component.view.FCLUILayout;

import java.io.File;
import java.util.ArrayList;

public class VersionSettingPage extends FCLCommonPage implements ManageUI.VersionLoadable, View.OnClickListener {

    private final boolean globalSetting;

    private VersionSetting lastVersionSetting = null;
    private Profile profile;
    private WeakListenerHolder listenerHolder;
    private String versionId;

    private FCLEditText txtWidth;
    private FCLEditText txtHeight;
    private FCLEditText txtJVMArgs;
    private FCLEditText txtGameArgs;
    private FCLEditText txtMetaspace;
    private FCLEditText txtServerIP;

    private FCLCheckBox chkAutoAllocate;
    private FCLCheckBox chkFullscreen;

    private FCLImageView iconView;

    private FCLSeekBar allocateSeekbar;

    private FCLSwitch isolateWorkingDirSwitch;
    private FCLSwitch beGestureSwitch;
    private FCLSwitch noGameCheckSwitch;
    private FCLSwitch noJVMCheckSwitch;

    private FCLSpinner<Integer> javaSpinner;
    private FCLSpinner<ProcessPriority> processPrioritySpinner;
    private FCLSpinner<FCLConfig.Renderer> rendererSpinner;

    private FCLImageButton editIconButton;
    private FCLImageButton deleteIconButton;
    private FCLImageButton controllerButton;

    private final InvalidationListener specificSettingsListener;
    private final StringProperty selectedVersion = new SimpleStringProperty();
    private final BooleanProperty enableSpecificSettings = new SimpleBooleanProperty(false);
    private final IntegerProperty maxMemory = new SimpleIntegerProperty();
    private final IntegerProperty usedMemory = new SimpleIntegerProperty(0);
    private final BooleanProperty modpack = new SimpleBooleanProperty();

    public VersionSettingPage(Context context, int id, FCLUILayout parent, int resId, boolean globalSetting) {
        super(context, id, parent, resId);
        this.globalSetting = globalSetting;
        create();
        specificSettingsListener = any -> enableSpecificSettings.set(!lastVersionSetting.isUsesGlobal());
    }

    private void create() {
        FCLLinearLayout settingTypeLayout = findViewById(R.id.special_setting_layout);
        FCLLinearLayout settingLayout = findViewById(R.id.setting_layout);

        txtWidth = findViewById(R.id.edit_width);
        txtHeight = findViewById(R.id.edit_height);
        txtJVMArgs = findViewById(R.id.edit_jvm_args);
        txtGameArgs = findViewById(R.id.edit_minecraft_args);
        txtMetaspace = findViewById(R.id.edit_permgen_space);
        txtServerIP = findViewById(R.id.edit_server);

        chkAutoAllocate = findViewById(R.id.edit_auto_allocate);
        chkFullscreen = findViewById(R.id.edit_fullscreen);

        iconView = findViewById(R.id.icon);

        allocateSeekbar = findViewById(R.id.edit_memory);

        FCLSwitch specialSettingSwitch = findViewById(R.id.enable_per_instance_setting);
        specialSettingSwitch.addCheckedChangeListener();
        isolateWorkingDirSwitch = findViewById(R.id.edit_game_dir);
        beGestureSwitch = findViewById(R.id.edit_controller_injector);
        noGameCheckSwitch = findViewById(R.id.edit_not_check_game);
        noJVMCheckSwitch = findViewById(R.id.edit_not_check_java);

        javaSpinner = findViewById(R.id.edit_java);
        processPrioritySpinner = findViewById(R.id.edit_process_priority);
        rendererSpinner = findViewById(R.id.edit_renderer);

        // add spinner data
        ArrayList<Integer> javaVersionDataList = new ArrayList<>();
        javaVersionDataList.add(JavaVersion.JAVA_AUTO.getId());
        javaVersionDataList.add(JavaVersion.JAVA_8.getId());
        javaVersionDataList.add(JavaVersion.JAVA_17.getId());
        javaSpinner.setDataList(javaVersionDataList);

        ArrayList<ProcessPriority> processPriorityDataList = new ArrayList<>();
        processPriorityDataList.add(ProcessPriority.LOW);
        processPriorityDataList.add(ProcessPriority.NORMAL);
        processPriorityDataList.add(ProcessPriority.HIGH);
        processPrioritySpinner.setDataList(processPriorityDataList);

        ArrayList<FCLConfig.Renderer> rendererDataList = new ArrayList<>();
        rendererDataList.add(FCLConfig.Renderer.RENDERER_GL4ES);
        rendererDataList.add(FCLConfig.Renderer.RENDERER_ZINK);
        rendererDataList.add(FCLConfig.Renderer.RENDERER_ANGLE);
        rendererSpinner.setDataList(rendererDataList);

        // add spinner text
        ArrayList<String> javaVersionList = new ArrayList<>();
        javaVersionList.add(getContext().getString(R.string.settings_game_java_version_auto));
        javaVersionList.add("JRE 8");
        javaVersionList.add("JRE 17");
        ArrayAdapter<String> javaAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, javaVersionList);
        javaAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        javaSpinner.setAdapter(javaAdapter);

        ArrayList<String> processPriorityList = new ArrayList<>();
        processPriorityList.add(getContext().getString(R.string.settings_advanced_process_priority_low));
        processPriorityList.add(getContext().getString(R.string.settings_advanced_process_priority_normal));
        processPriorityList.add(getContext().getString(R.string.settings_advanced_process_priority_high));
        ArrayAdapter<String> processPriorityAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, processPriorityList);
        processPriorityAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        processPrioritySpinner.setAdapter(processPriorityAdapter);

        ArrayList<String> rendererList = new ArrayList<>();
        rendererList.add(getContext().getString(R.string.settings_fcl_renderer_gl4es));
        rendererList.add(getContext().getString(R.string.settings_fcl_renderer_virgl));
        rendererList.add(getContext().getString(R.string.settings_fcl_renderer_angle));
        ArrayAdapter<String> rendererAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, rendererList);
        rendererAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        rendererSpinner.setAdapter(rendererAdapter);

        editIconButton = findViewById(R.id.edit_icon);
        deleteIconButton = findViewById(R.id.delete_icon);
        controllerButton = findViewById(R.id.edit_controller);

        editIconButton.setOnClickListener(this);
        deleteIconButton.setOnClickListener(this);
        controllerButton.setOnClickListener(this);

        FCLProgressBar memoryBar = findViewById(R.id.memory_bar);

        FCLTextView memoryStateText = findViewById(R.id.memory_state);
        FCLTextView memoryText = findViewById(R.id.memory_text);
        FCLTextView memoryInfoText = findViewById(R.id.memory_info_text);
        FCLTextView memoryAllocateText = findViewById(R.id.memory_allocate_text);

        memoryStateText.stringProperty().bind(Bindings.createStringBinding(() -> {
            if (chkAutoAllocate.isChecked()) {
                return getContext().getString(R.string.settings_memory_lower_bound);
            } else {
                return getContext().getString(R.string.settings_memory);
            }
        }, chkAutoAllocate.checkProperty()));

        allocateSeekbar.setMax(MemoryUtils.getTotalDeviceMemory(getContext()));
        memoryBar.setMax(MemoryUtils.getTotalDeviceMemory(getContext()));

        allocateSeekbar.addProgressListener();
        allocateSeekbar.progressProperty().bindBidirectional(maxMemory);

        memoryText.stringProperty().bind(Bindings.createStringBinding(() -> allocateSeekbar.progressProperty().intValue() + " MB", allocateSeekbar.progressProperty()));

        memoryBar.firstProgressProperty().bind(usedMemory);
        memoryBar.secondProgressProperty().bind(Bindings.createIntegerBinding(() -> {
            int allocate = (int) (FCLGameRepository.getAllocatedMemory(maxMemory.intValue() * 1024L * 1024L, MemoryUtils.getFreeDeviceMemory(getContext()) * 1024L * 1024L, chkAutoAllocate.isChecked()) / 1024. / 1024);
            return usedMemory.intValue() + (chkAutoAllocate.isChecked() ? allocate : maxMemory.intValue());
        }, usedMemory, maxMemory, chkAutoAllocate.checkProperty()));

        memoryInfoText.stringProperty().bind(Bindings.createStringBinding(() -> AndroidUtils.getLocalizedText(getContext(), "settings_memory_used_per_total", MemoryUtils.getUsedDeviceMemory(getContext()) / 1024., MemoryUtils.getTotalDeviceMemory(getContext()) / 1024.), usedMemory));

        memoryAllocateText.stringProperty().bind(Bindings.createStringBinding(() -> {
            long maxMemory = Lang.parseInt(this.maxMemory.get(), 0) * 1024L * 1024L;
            return AndroidUtils.getLocalizedText(getContext(), maxMemory / 1024. / 1024. > MemoryUtils.getFreeDeviceMemory(getContext())
                            ? (chkAutoAllocate.isChecked() ? "settings_memory_allocate_auto_exceeded" : "settings_memory_allocate_manual_exceeded")
                            : (chkAutoAllocate.isChecked() ? "settings_memory_allocate_auto" : "settings_memory_allocate_manual"),
                    maxMemory / 1024. / 1024. / 1024.,
                    FCLGameRepository.getAllocatedMemory(maxMemory, MemoryUtils.getFreeDeviceMemory(getContext()) * 1024L * 1024L, chkAutoAllocate.isChecked()) / 1024. / 1024. / 1024.,
                    MemoryUtils.getFreeDeviceMemory(getContext()) / 1024.);
        }, usedMemory, maxMemory, chkAutoAllocate.checkProperty()));

        settingTypeLayout.setVisibility(globalSetting ? View.GONE : View.VISIBLE);

        if (!globalSetting) {
            specialSettingSwitch.disableProperty().bind(modpack);
            specialSettingSwitch.checkProperty().bindBidirectional(enableSpecificSettings);
            settingLayout.visibilityProperty().bind(enableSpecificSettings);
        }

        enableSpecificSettings.addListener((a, b, newValue) -> {
            if (versionId == null) return;

            // do not call versionSettings.setUsesGlobal(true/false)
            // because versionSettings can be the global one.
            // global versionSettings.usesGlobal is always true.
            if (newValue)
                profile.getRepository().specializeVersionSetting(versionId);
            else
                profile.getRepository().globalizeVersionSetting(versionId);

            Schedulers.androidUIThread().execute(() -> loadVersion(profile, versionId));
        });
    }

    @Override
    public Task<?> refresh(Object... param) {
        return null;
    }

    @Override
    public void onResume() {
        super.onResume();
        usedMemory.set(MemoryUtils.getUsedDeviceMemory(getContext()));
    }

    @Override
    public void loadVersion(Profile profile, String versionId) {
        this.profile = profile;
        this.versionId = versionId;
        this.listenerHolder = new WeakListenerHolder();

        if (versionId == null) {
            enableSpecificSettings.set(true);
            listenerHolder.add(FXUtils.onWeakChangeAndOperate(profile.selectedVersionProperty(), this.selectedVersion::setValue));
        }

        VersionSetting versionSetting = profile.getVersionSetting(versionId);

        modpack.set(versionId != null && profile.getRepository().isModpack(versionId));
        usedMemory.set(MemoryUtils.getUsedDeviceMemory(getContext()));

        // unbind data fields
        if (lastVersionSetting != null) {
            FXUtils.unbind(txtWidth, lastVersionSetting.widthProperty());
            FXUtils.unbind(txtHeight, lastVersionSetting.heightProperty());
            FXUtils.unbind(txtJVMArgs, lastVersionSetting.javaArgsProperty());
            FXUtils.unbind(txtGameArgs, lastVersionSetting.minecraftArgsProperty());
            FXUtils.unbind(txtMetaspace, lastVersionSetting.permSizeProperty());
            FXUtils.unbind(txtServerIP, lastVersionSetting.serverIpProperty());
            FXUtils.unbindBoolean(chkAutoAllocate, lastVersionSetting.autoMemoryProperty());
            FXUtils.unbindBoolean(chkFullscreen, lastVersionSetting.fullscreenProperty());
            FXUtils.unbindBoolean(isolateWorkingDirSwitch, lastVersionSetting.isolateGameDirProperty());
            FXUtils.unbindBoolean(noGameCheckSwitch, lastVersionSetting.notCheckGameProperty());
            FXUtils.unbindBoolean(noJVMCheckSwitch, lastVersionSetting.notCheckJVMProperty());
            FXUtils.unbindBoolean(beGestureSwitch, lastVersionSetting.beGestureProperty());
            FXUtils.unbindSelection(javaSpinner, lastVersionSetting.javaProperty());
            FXUtils.unbindSelection(processPrioritySpinner, lastVersionSetting.processPriorityProperty());
            FXUtils.unbindSelection(rendererSpinner, lastVersionSetting.rendererProperty());
            maxMemory.unbindBidirectional(lastVersionSetting.maxMemoryProperty());

            lastVersionSetting.usesGlobalProperty().removeListener(specificSettingsListener);
        }

        // bind new data fields
        FXUtils.bindInt(txtWidth, versionSetting.widthProperty());
        FXUtils.bindInt(txtHeight, versionSetting.heightProperty());
        FXUtils.bindString(txtJVMArgs, versionSetting.javaArgsProperty());
        FXUtils.bindString(txtGameArgs, versionSetting.minecraftArgsProperty());
        FXUtils.bindString(txtMetaspace, versionSetting.permSizeProperty());
        FXUtils.bindString(txtServerIP, versionSetting.serverIpProperty());
        FXUtils.bindBoolean(chkAutoAllocate, versionSetting.autoMemoryProperty());
        FXUtils.bindBoolean(chkFullscreen, versionSetting.fullscreenProperty());
        FXUtils.bindBoolean(isolateWorkingDirSwitch, versionSetting.isolateGameDirProperty());
        FXUtils.bindBoolean(noGameCheckSwitch, versionSetting.notCheckGameProperty());
        FXUtils.bindBoolean(noJVMCheckSwitch, versionSetting.notCheckJVMProperty());
        FXUtils.bindBoolean(beGestureSwitch, versionSetting.beGestureProperty());
        FXUtils.bindSelection(javaSpinner, versionSetting.javaProperty());
        FXUtils.bindSelection(processPrioritySpinner, versionSetting.processPriorityProperty());
        FXUtils.bindSelection(rendererSpinner, versionSetting.rendererProperty());
        maxMemory.bindBidirectional(versionSetting.maxMemoryProperty());

        versionSetting.usesGlobalProperty().addListener(specificSettingsListener);
        if (versionId != null)
            enableSpecificSettings.set(!versionSetting.isUsesGlobal());

        lastVersionSetting = versionSetting;

        loadIcon();
    }

    private void onExploreIcon() {
        if (versionId == null)
            return;

        SelectIconDialog dialog = new SelectIconDialog(getContext());
        dialog.show();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void onDeleteIcon() {
        if (versionId == null)
            return;

        File iconFile = profile.getRepository().getVersionIconFile(versionId);
        if (iconFile.exists())
            iconFile.delete();
        loadIcon();
    }

    private void loadIcon() {
        if (versionId == null) {
            return;
        }

        iconView.setImageDrawable(profile.getRepository().getVersionIconImage(versionId));
    }

    @Override
    public void onClick(View view) {
        if (view == editIconButton) {
            onExploreIcon();
        }
        if (view == deleteIconButton) {
            onDeleteIcon();
        }
        if (view == controllerButton) {

        }
    }
}
