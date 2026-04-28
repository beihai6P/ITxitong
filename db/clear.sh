#!/bin/bash

echo "正在清理数据库..."
echo "注意：此操作将删除itoms数据库及其所有数据！"
read -p "按Enter继续，按Ctrl+C退出..."

echo "删除数据库..."
mysql -u root -e "DROP DATABASE IF EXISTS itoms;"

echo "创建新数据库..."
mysql -u root -e "CREATE DATABASE IF NOT EXISTS itoms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"

echo "初始化数据库结构..."
mysql -u root itoms < full_init.sql

echo "数据库清理完成！"