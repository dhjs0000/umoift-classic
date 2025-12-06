package com.infiniteflameteam.umoift.commands;

import com.infiniteflameteam.umoift.claim.ClaimData;
import com.infiniteflameteam.umoift.claim.ClaimManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ClaimCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimCommand.class);
    private static final SuggestionProvider<CommandSourceStack> CLAIM_SUGGESTIONS =
            (context, builder) -> {
                ServerPlayer player = context.getSource().getPlayer();
                if (player != null) {
                    List<String> allClaims = ClaimManager.getInstance().getAllClaimNames();
                    for (String claimName : allClaims) {
                        builder.suggest(claimName);
                    }
                }
                return builder.buildFuture();
            };

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claim")
                .requires(source -> source.isPlayer())

                .then(Commands.literal("addclaim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .then(Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                .executes(context -> executeAddClaim(
                                                        context,
                                                        StringArgumentType.getString(context, "claimName"),
                                                        BlockPosArgument.getLoadedBlockPos(context, "from"),
                                                        BlockPosArgument.getLoadedBlockPos(context, "to")
                                                ))
                                        )
                                )
                        )
                )

                .then(Commands.literal("listclaim")
                        .executes(context -> executeListClaim(context))
                )

                .then(Commands.literal("queryclaim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .executes(context -> executeQueryClaim(
                                        context,
                                        StringArgumentType.getString(context, "claimName")
                                ))
                        )
                )

                .then(Commands.literal("removeclaim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .executes(context -> executeRemoveClaim(
                                        context,
                                        StringArgumentType.getString(context, "claimName")
                                ))
                        )
                )

                .then(Commands.literal("changeclaim")
                        .then(Commands.argument("oldClaimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .then(Commands.argument("newClaimName", StringArgumentType.string())
                                        .then(Commands.argument("newFrom", BlockPosArgument.blockPos())
                                                .then(Commands.argument("newTo", BlockPosArgument.blockPos())
                                                        .then(Commands.argument("oldOwner", EntityArgument.player())
                                                                .then(Commands.argument("newOwner", EntityArgument.player())
                                                                        .executes(context -> executeChangeClaim(
                                                                                context,
                                                                                StringArgumentType.getString(context, "oldClaimName"),
                                                                                StringArgumentType.getString(context, "newClaimName"),
                                                                                BlockPosArgument.getLoadedBlockPos(context, "newFrom"),
                                                                                BlockPosArgument.getLoadedBlockPos(context, "newTo"),
                                                                                EntityArgument.getPlayer(context, "oldOwner"),
                                                                                EntityArgument.getPlayer(context, "newOwner")
                                                                        ))
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("setpermissionstoclaim")
                        .then(Commands.argument("claimName", StringArgumentType.string())
                                .suggests(CLAIM_SUGGESTIONS)
                                .then(Commands.argument("targetPlayer", EntityArgument.player())
                                        .then(Commands.argument("permission", StringArgumentType.string())
                                                .then(Commands.argument("value", BoolArgumentType.bool())
                                                        .executes(context -> executeSetPermissions(
                                                                context,
                                                                StringArgumentType.getString(context, "claimName"),
                                                                EntityArgument.getPlayer(context, "targetPlayer"),
                                                                StringArgumentType.getString(context, "permission"),
                                                                BoolArgumentType.getBool(context, "value")
                                                        ))
                                                )
                                        )
                                )
                        )
                )

                .then(Commands.literal("op")
                        .then(Commands.argument("targetPlayer", EntityArgument.player())
                                .executes(context -> executeOp(
                                        context,
                                        EntityArgument.getPlayer(context, "targetPlayer")
                                ))
                        )
                )

                .then(Commands.literal("deop")
                        .then(Commands.argument("targetPlayer", EntityArgument.player())
                                .executes(context -> executeDeop(
                                        context,
                                        EntityArgument.getPlayer(context, "targetPlayer")
                                ))
                        )
                )

                .then(Commands.literal("help")
                        .executes(context -> executeHelp(context))
                )
        );
    }

    private static int executeAddClaim(CommandContext<CommandSourceStack> context, String claimName, BlockPos from, BlockPos to) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            boolean success = ClaimManager.getInstance().createClaim(claimName, player, from, to);
            if (success) {
                final String finalClaimName = claimName;
                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功创建领地: §e%s", finalClaimName)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c创建领地失败: 领地名已存在或位置重叠"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("创建领地失败", e);
            context.getSource().sendFailure(Component.literal("§c创建领地失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeListClaim(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            List<ClaimData> playerClaims = ClaimManager.getInstance().getPlayerClaims(player);
            List<String> allClaims = ClaimManager.getInstance().getAllClaimNames();

            final int playerClaimsSize = playerClaims.size();
            final int allClaimsSize = allClaims.size();

            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§6=== 领地列表 ===\n§e总领地数: §f%d\n§e你的领地: §f%d",
                            allClaimsSize, playerClaimsSize)
            ), false);

            if (!playerClaims.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.literal("§e你的领地列表:"), false);
                for (int i = 0; i < playerClaims.size(); i++) {
                    final ClaimData claim = playerClaims.get(i);
                    final int index = i + 1;
                    final BlockPos from = claim.getFrom();
                    final BlockPos to = claim.getTo();

                    context.getSource().sendSuccess(() -> Component.literal(
                            String.format("§7%d. §f%s §7(位置: %d,%d,%d 到 %d,%d,%d)",
                                    index, claim.getClaimName(),
                                    from.getX(), from.getY(), from.getZ(),
                                    to.getX(), to.getY(), to.getZ())
                    ), false);
                }
            }

            return 1;
        } catch (Exception e) {
            LOGGER.error("列出领地失败", e);
            context.getSource().sendFailure(Component.literal("§c列出领地失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeQueryClaim(CommandContext<CommandSourceStack> context, String claimName) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            ClaimData claim = ClaimManager.getInstance().getClaim(claimName);
            if (claim == null) {
                context.getSource().sendFailure(Component.literal("§c未找到领地: " + claimName));
                return 0;
            }

            final ClaimData finalClaim = claim;
            final String finalClaimName = claim.getClaimName();
            final String finalOwnerName = claim.getOwnerName();
            final BlockPos from = claim.getFrom();
            final BlockPos to = claim.getTo();

            context.getSource().sendSuccess(() -> Component.literal(
                    String.format("§6=== 领地信息 ===\n§e领地名称: §f%s\n§e主人: §f%s\n§e起始位置: §f(%d,%d,%d)\n§e终止位置: §f(%d,%d,%d)",
                            finalClaimName,
                            finalOwnerName,
                            from.getX(), from.getY(), from.getZ(),
                            to.getX(), to.getY(), to.getZ())
            ), false);

            Set<UUID> privilegedPlayers = finalClaim.getPlayersWithPermissions();
            if (!privilegedPlayers.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.literal("§e有特权的玩家:"), false);
                List<UUID> privilegedList = new ArrayList<>(privilegedPlayers);
                for (int i = 0; i < privilegedList.size(); i++) {
                    final UUID playerId = privilegedList.get(i);
                    final String playerIdStr = playerId.toString().substring(0, 8);

                    context.getSource().sendSuccess(() -> Component.literal(
                            String.format("§7  - §f%s §7(UUID: %s)",
                                    "玩家", playerIdStr)
                    ), false);
                }
            }

            context.getSource().sendSuccess(() -> Component.literal("§e权限设置:"), false);
            for (UUID playerId : privilegedPlayers) {
                final UUID finalPlayerId = playerId;
                final String playerIdStr = playerId.toString().substring(0, 8);
                final Map<String, Boolean> perms = new HashMap<>(finalClaim.getAllPermissions(finalPlayerId));

                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§7玩家 %s:", playerIdStr)
                ), false);

                List<Map.Entry<String, Boolean>> permEntries = new ArrayList<>(perms.entrySet());
                for (int i = 0; i < permEntries.size(); i++) {
                    final Map.Entry<String, Boolean> entry = permEntries.get(i);

                    context.getSource().sendSuccess(() -> Component.literal(
                            String.format("§7  %s: §f%s", entry.getKey(), entry.getValue() ? "是" : "否")
                    ), false);
                }
            }

            return 1;
        } catch (Exception e) {
            LOGGER.error("查询领地失败", e);
            context.getSource().sendFailure(Component.literal("§c查询领地失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeRemoveClaim(CommandContext<CommandSourceStack> context, String claimName) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            boolean success = ClaimManager.getInstance().removeClaim(claimName, player);
            if (success) {
                final String finalClaimName = claimName;
                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功删除领地: §e%s", finalClaimName)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal(
                        "§c删除领地失败: 领地不存在或你没有权限"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("删除领地失败", e);
            context.getSource().sendFailure(Component.literal("§c删除领地失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeChangeClaim(CommandContext<CommandSourceStack> context,
                                          String oldClaimName, String newClaimName,
                                          BlockPos newFrom, BlockPos newTo,
                                          ServerPlayer oldOwner, ServerPlayer newOwner) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            boolean success = ClaimManager.getInstance().modifyClaim(
                    oldClaimName, newClaimName, newFrom, newTo, newOwner, player
            );

            if (success) {
                final String finalOldClaimName = oldClaimName;
                final String finalNewClaimName = newClaimName;
                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功修改领地: §e%s §a-> §e%s", finalOldClaimName, finalNewClaimName)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal(
                        "§c修改领地失败: 领地不存在、新领地名已存在、位置重叠或你没有权限"
                ));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("修改领地失败", e);
            context.getSource().sendFailure(Component.literal("§c修改领地失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeSetPermissions(CommandContext<CommandSourceStack> context,
                                             String claimName, ServerPlayer targetPlayer,
                                             String permission, boolean value) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            ClaimData claim = ClaimManager.getInstance().getClaim(claimName);
            if (claim == null) {
                context.getSource().sendFailure(Component.literal("§c未找到领地: " + claimName));
                return 0;
            }

            if (!claim.canModify(player.getUUID()) && !ClaimManager.getInstance().canUseClaimCommand(player)) {
                context.getSource().sendFailure(Component.literal("§c你没有权限设置这个领地的权限"));
                return 0;
            }

            if (targetPlayer.getUUID().equals(claim.getOwnerId())) {
                context.getSource().sendFailure(Component.literal("§c不能修改领地主人的权限"));
                return 0;
            }

            boolean success = claim.setPermission(targetPlayer.getUUID(), permission, value);
            if (success) {
                final String targetPlayerName = targetPlayer.getScoreboardName();
                final String finalPermission = permission;
                final String finalValue = value ? "开启" : "关闭";

                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功设置玩家 §e%s §a的权限 §e%s §a为: §f%s",
                                targetPlayerName, finalPermission, finalValue)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c设置权限失败"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("设置权限失败", e);
            context.getSource().sendFailure(Component.literal("§c设置权限失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeOp(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            if (!ClaimManager.getInstance().canUseClaimCommand(player)) {
                context.getSource().sendFailure(Component.literal("§c你没有权限使用这个命令"));
                return 0;
            }

            boolean success = ClaimManager.getInstance().addGlobalOp(targetPlayer);
            if (success) {
                final String targetPlayerName = targetPlayer.getScoreboardName();

                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功添加全局OP: §e%s", targetPlayerName)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c该玩家已经是全局OP"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("添加全局OP失败", e);
            context.getSource().sendFailure(Component.literal("§c添加全局OP失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeDeop(CommandContext<CommandSourceStack> context, ServerPlayer targetPlayer) {
        try {
            ServerPlayer player = context.getSource().getPlayer();
            if (player == null) {
                context.getSource().sendFailure(Component.literal("只有玩家可以执行此命令"));
                return 0;
            }

            if (!ClaimManager.getInstance().canUseClaimCommand(player)) {
                context.getSource().sendFailure(Component.literal("§c你没有权限使用这个命令"));
                return 0;
            }

            boolean success = ClaimManager.getInstance().removeGlobalOp(targetPlayer);
            if (success) {
                final String targetPlayerName = targetPlayer.getScoreboardName();

                context.getSource().sendSuccess(() -> Component.literal(
                        String.format("§a成功移除全局OP: §e%s", targetPlayerName)
                ), false);
                return 1;
            } else {
                context.getSource().sendFailure(Component.literal("§c该玩家不是全局OP"));
                return 0;
            }
        } catch (Exception e) {
            LOGGER.error("移除全局OP失败", e);
            context.getSource().sendFailure(Component.literal("§c移除全局OP失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeHelp(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal(
                "§6=== 领地命令帮助 ===\n" +
                        "§e/claim addclaim <领地名> <from> <to> §7- 创建领地\n" +
                        "§e/claim listclaim §7- 列出所有领地\n" +
                        "§e/claim queryclaim <领地名> §7- 查询领地详细信息\n" +
                        "§e/claim removeclaim <领地名> §7- 删除领地（需权限）\n" +
                        "§e/claim changeclaim <旧领地名> <新领地名> <新from> <新to> <旧主人> <新主人> §7- 修改领地（需权限）\n" +
                        "§e/claim setpermissionstoclaim <领地名> <玩家> <权限名> <true|false> §7- 设置玩家权限（需权限）\n" +
                        "§e/claim op <玩家> §7- 添加全局OP（需管理员权限）\n" +
                        "§e/claim deop <玩家> §7- 移除全局OP（需管理员权限）\n" +
                        "§e/claim help §7- 显示帮助信息\n\n" +
                        "§6可用权限名: §7build, break, interact, use, pvp, explosion, fire_spread, mob_griefing"
        ), false);
        return 1;
    }
}