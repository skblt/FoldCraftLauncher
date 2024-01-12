/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2020  huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.tungsten.fclcore.event;

/**
 * This event gets fired when loading versions in a .minecraft folder.
 * <br>
 * This event is fired on the {@link com.tungsten.fclcore.event.EventBus#EVENT_BUS}
 */
public final class RefreshingVersionsEvent extends Event {

    /**
     * Constructor.
     *
     * @param source {@link com.tungsten.fclcore.game.GameRepository}
     */
    public RefreshingVersionsEvent(Object source) {
        super(source);
    }

    @Override
    public boolean hasResult() {
        return true;
    }
}
